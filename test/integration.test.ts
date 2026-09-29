import 'reflect-metadata';
import assert from 'node:assert/strict';
import { createHmac } from 'node:crypto';
import path from 'node:path';
import { after, before, beforeEach, describe, it } from 'node:test';
import {
  CreateBucketCommand,
  GetObjectCommand,
  S3Client,
} from '@aws-sdk/client-s3';
import { INestApplication } from '@nestjs/common';
import { Test } from '@nestjs/testing';
import { MySqlContainer, StartedMySqlContainer } from '@testcontainers/mysql';
import request from 'supertest';
import {
  GenericContainer,
  Network,
  StartedNetwork,
  StartedTestContainer,
  Wait,
} from 'testcontainers';
import { DataSource } from 'typeorm';
import { loadSettings, Settings } from '../src/adapter/config/settings';
import { S3ImageStorage } from '../src/adapter/integration/storage/s3-image-storage';
import { AppModule } from '../src/app.module';
import { ApplicationNotifier } from '../src/application/apply/provided/application-notifier';
import { RecruitmentFinder } from '../src/application/recruitment/provided/recruitment-finder';
import { TokenIssuer } from '../src/application/user/required/token-issuer';
import { configureApp } from '../src/bootstrap';
import {
  Application,
  ApplicationInterviewTime,
  ClubActivity,
} from '../src/domain/apply/application';
import {
  ApplicationResultLog,
  NotificationType,
} from '../src/domain/apply/application-result-log';
import { Board, Reply } from '../src/domain/board/board';
import { BoardTemplate } from '../src/domain/board/board-template';
import { EmailAlarm } from '../src/domain/recruitment/email-alarm';
import {
  InterviewTime,
  Recruitment,
} from '../src/domain/recruitment/recruitment';
import { User, UserClass, UserRole } from '../src/domain/user/user';
import { CaptureLogger } from './support/capture-logger';
import { ExternalServices } from './support/external-services';

describe('NestJS integration with real MySQL and Flyway', {
  timeout: 240000,
}, () => {
  let network: StartedNetwork | undefined;
  let mysql: StartedMySqlContainer | undefined;
  let app: INestApplication | undefined;
  let db: DataSource;
  let tokens: TokenIssuer;
  let minio: StartedTestContainer | undefined;
  let storage: S3Client | undefined;
  const external = new ExternalServices();
  const logs = new CaptureLogger();

  before(async () => {
    await external.start();
    network = await new Network().start();
    mysql = await new MySqlContainer('mysql:8.4')
      .withDatabase('yg_integration')
      .withUsername('yg_test')
      .withUserPassword('yg_test_password')
      .withNetwork(network)
      .withNetworkAliases('mysql')
      .start();
    const flyway = await new GenericContainer('flyway/flyway:13.6.0-alpine')
      .withNetwork(network)
      .withCopyDirectoriesToContainer([
        {
          source: path.resolve('db/migration'),
          target: '/flyway/sql',
        },
      ])
      .withEnvironment({
        FLYWAY_URL:
          'jdbc:mysql://mysql:3306/yg_integration?allowPublicKeyRetrieval=true&useSSL=false',
        FLYWAY_USER: 'yg_test',
        FLYWAY_PASSWORD: 'yg_test_password',
        FLYWAY_CLEAN_DISABLED: 'true',
      })
      .withCommand(['migrate'])
      .withWaitStrategy(Wait.forOneShotStartup())
      .withStartupTimeout(120000)
      .start();
    await flyway.stop();
    minio = await new GenericContainer(
      'pgsty/minio:RELEASE.2026-08-04T00-00-00Z',
    )
      .withEnvironment({
        MINIO_ROOT_USER: 'test-user',
        MINIO_ROOT_PASSWORD: 'test-password',
      })
      .withCommand(['server', '/data'])
      .withExposedPorts(9000)
      .withWaitStrategy(Wait.forHttp('/minio/health/live', 9000))
      .start();
    const endpoint = `http://${minio.getHost()}:${minio.getMappedPort(9000)}`;
    storage = new S3Client({
      endpoint,
      region: 'ap-northeast-2',
      forcePathStyle: true,
      credentials: {
        accessKeyId: 'test-user',
        secretAccessKey: 'test-password',
      },
      requestChecksumCalculation: 'WHEN_REQUIRED',
    });
    await storage.send(new CreateBucketCommand({ Bucket: 'test-bucket' }));
    const settings = loadSettings({
      DATABASE_URL: `mysql://${mysql.getHost()}:${mysql.getPort()}/yg_integration`,
      DATABASE_USERNAME: mysql.getUsername(),
      DATABASE_PASSWORD: mysql.getUserPassword(),
      JWT_SECRET_KEY: Buffer.alloc(32, 't').toString('base64'),
      KAKAO_CLIENT_ID: 'test',
      KAKAO_CLIENT_SECRET: 'test',
      KAKAO_TOKEN_URL: `${external.kakaoUrl}/oauth/token`,
      KAKAO_LOGIN_URI: `${external.kakaoUrl}/v2/user/me`,
      SMTP_HOST: '127.0.0.1',
      SMTP_PORT: String(external.smtpPort),
      AWS_S3_BUCKET: 'test-bucket',
      AWS_S3_PUBLIC_URL: endpoint,
      APP_PROFILE: 'home',
      S3_ENDPOINT: endpoint,
      AWS_ACCESS_KEY_ID: 'test-user',
      AWS_SECRET_ACCESS_KEY: 'test-password',
    });
    const module = await Test.createTestingModule({
      imports: [AppModule.register(settings)],
    }).compile();
    app = module.createNestApplication();
    app.useLogger(logs);
    configureApp(app, settings);
    await app.init();
    db = app.get(DataSource);
    tokens = app.get(TokenIssuer);
  });

  beforeEach(async () => {
    assert.equal(db.options.database, 'yg_integration');
    for (const table of [
      'application_available_interview_time',
      'club_activity',
      'application_result_log',
      'application',
      'interview_time',
      'recruitment_period',
      'email_alarm',
      'reply',
      'board',
      'board_template',
      'user',
    ])
      await db.query(`DELETE FROM \`${table}\``);
    external.messages.length = 0;
    external.kakaoRequests.length = 0;
    external.rejectMail = false;
  });

  after(async () => {
    await app?.close();
    await external.stop();
    storage?.destroy();
    await minio?.stop();
    await mysql?.stop();
    await network?.stop();
  });

  const http = () => {
    assert.ok(app);
    return request(app.getHttpServer());
  };
  const fixtureUser = async (role = UserRole.MEMBER, kakaoId = '12345') => {
    const repository = db.getRepository(User);
    return repository.save(
      repository.create({
        kakaoId,
        name: '테스트',
        phoneNumber: '010-1234-5678',
        studentId: 20,
        major: '컴퓨터과학',
        semester: 40,
        role,
        userClass: UserClass.YB,
      }),
    );
  };
  const signup = {
    name: '홍길동',
    phoneNumber: '010-0000-0000',
    studentId: 24,
    major: '경영학',
    semester: 40,
  };
  const recruitmentDto = {
    semester: 40,
    startDate: '2026-09-01',
    endDate: '2026-09-30',
    firstResultDate: '2026-10-01',
    interviewStartDate: '2026-10-02',
    interviewEndDate: '2026-10-04',
    finalResultDate: '2026-10-05',
    orientationDate: '2026-10-10',
  };
  const applicationDto = {
    name: '지원자',
    photoKey: 'store-image/test-photo.jpg',
    birthDate: '2003-01-02',
    studentId: 2023123456,
    email: 'applicant@example.test',
    major: '컴퓨터과학',
    phoneNumber: '010-1111-2222',
    selfIntroduction: '안녕하세요',
    applyReason: '골프를 배우고 싶습니다',
    skillEvaluation: '입문',
    golfMemory: '첫 라운드',
    swingVideo: '',
    semester: 40,
    activityClubs: [
      {
        clubName: '개발동아리',
        startDate: '2025-03-01',
        endDate: '2025-12-31',
        role: '회원',
      },
    ],
    availableInterviewTimeIds: [] as number[],
  };
  const fixtureRecruitment = async (semester = 40) =>
    db
      .getRepository(Recruitment)
      .save(
        db.getRepository(Recruitment).create({ ...recruitmentDto, semester }),
      );

  it('exchanges Kakao tokens over HTTP, signs up, signs in, refreshes and logs out', async () => {
    const oauth = await http()
      .post('/oauth/kakao')
      .send({ kakaoCode: 'valid-code' })
      .expect(200);
    assert.match(
      String(oauth.headers['set-cookie']),
      /refreshToken=kakao-refresh/,
    );
    assert.match(String(oauth.headers['set-cookie']), /HttpOnly/);
    assert.match(String(oauth.headers['set-cookie']), /Secure/);
    const oauthHeader = `Bearer ${oauth.body.data.accessToken}`;
    await http()
      .post('/users/signUp')
      .set('Authorization', oauthHeader)
      .send(signup)
      .expect(200);
    const saved = await db
      .getRepository(User)
      .findOneByOrFail({ kakaoId: '45678' });
    assert.equal(saved.name, signup.name);
    assert.equal(saved.role, UserRole.MEMBER);
    assert.equal(saved.userClass, UserClass.NONE);
    const login = await http()
      .post('/users/signIn')
      .set('Authorization', oauthHeader)
      .expect(200);
    await http()
      .post('/users/loggedIn')
      .set('Authorization', `Bearer ${login.body.data.accessToken}`)
      .expect(200);
    await http()
      .post('/users/signIn/refresh')
      .set('Cookie', 'refreshToken=kakao-refresh')
      .expect(200);
    assert.equal(external.kakaoRequests[1]?.get('grant_type'), 'refresh_token');
    const logout = await http()
      .post('/users/logout')
      .set('Authorization', `Bearer ${login.body.data.accessToken}`)
      .expect(200);
    assert.match(
      String(logout.headers['set-cookie']),
      /Expires=Thu, 01 Jan 1970/,
    );
  });

  it('updates an existing unlinked member without losing their role or creating another row', async () => {
    const old = await fixtureUser(UserRole.LEADER, '0');
    const header = `Bearer ${tokens.issueOAuthToken('999')}`;
    await http()
      .post('/users/signUp')
      .set('Authorization', header)
      .send({ ...signup, name: old.name, studentId: old.studentId })
      .expect(200);
    const reloaded = await db
      .getRepository(User)
      .findOneByOrFail({ id: old.id });
    assert.equal(reloaded.kakaoId, '999');
    assert.equal(reloaded.role, UserRole.LEADER);
    assert.equal(reloaded.phoneNumber, signup.phoneNumber);
    assert.equal(await db.getRepository(User).count(), 1);
    await http()
      .post('/users/signUp')
      .set('Authorization', `Bearer ${tokens.issueOAuthToken('998')}`)
      .send({ ...signup, name: old.name, studentId: old.studentId })
      .expect(409);
  });

  it('rejects anonymous, forged, wrong-purpose and non-admin tokens before changing the database', async () => {
    const member = await fixtureUser();
    await http().get('/admin/users').expect(401);
    const token = tokens.issueAccessToken(member);
    const parts = token.split('.');
    parts[1] = Buffer.from(
      JSON.stringify({
        sub: 'login_member',
        userProfile: { id: member.id, name: 'forged', adminStatus: true },
      }),
    ).toString('base64url');
    await http()
      .get('/admin/users')
      .set('Authorization', `Bearer ${parts.join('.')}`)
      .expect(401);
    await http()
      .get('/admin/users')
      .set('Authorization', `Bearer ${token}`)
      .expect(403);
    await http()
      .post('/boards')
      .set('Authorization', `Bearer ${tokens.issueOAuthToken(member.id)}`)
      .send({ category: 'FREE', title: 'x', content: 'x' })
      .expect(401);
    await http()
      .post('/oauth/kakao')
      .send({ kakaoCode: 'invalid' })
      .expect(401);
    await http().post('/users/signIn/refresh').expect(401);
    assert.equal(await db.getRepository(Board).count(), 0);
  });

  it('accepts Spring-compatible JWTs without kind and rejects expired JWTs', async () => {
    const user = await fixtureUser(UserRole.LEADER);
    const header = Buffer.from(
      JSON.stringify({ typ: 'JWT', alg: 'HS256' }),
    ).toString('base64url');
    const legacyToken = (exp: number) => {
      const payload = Buffer.from(
        JSON.stringify({
          sub: 'login_member',
          exp,
          userProfile: {
            id: Number(user.id),
            name: user.name,
            adminStatus: true,
            memberStatus: true,
          },
        }),
      ).toString('base64url');
      const unsigned = `${header}.${payload}`;
      return `${unsigned}.${createHmac('sha256', Buffer.alloc(32, 't')).update(unsigned).digest('base64url')}`;
    };
    const valid = legacyToken(Math.floor(Date.now() / 1000) + 300);
    await http()
      .get('/admin/users')
      .set('Authorization', `Bearer ${valid}`)
      .expect(200);
    await http()
      .get('/admin/users')
      .set('Authorization', `Bearer ${legacyToken(1)}`)
      .expect(401);
    await db.getRepository(User).update(user.id, { role: UserRole.MEMBER });
    await http()
      .get('/admin/users')
      .set('Authorization', `Bearer ${valid}`)
      .expect(403);
    await db
      .getRepository(User)
      .update(user.id, { userClass: UserClass.BLACK_LIST });
    await http()
      .post('/users/loggedIn')
      .set('Authorization', `Bearer ${valid}`)
      .expect(403);
  });

  it('persists admin member-class changes and returns pagination and leaders', async () => {
    const admin = await fixtureUser(UserRole.LEADER);
    const member = await fixtureUser(UserRole.MEMBER, '56789');
    const header = `Bearer ${tokens.issueAccessToken(admin)}`;
    await http()
      .patch(`/admin/users/${member.id}`)
      .set('Authorization', header)
      .send({ userClass: 'OB' })
      .expect(200);
    assert.equal(
      (await db.getRepository(User).findOneByOrFail({ id: member.id }))
        .userClass,
      UserClass.OB,
    );
    const list = await http()
      .get('/admin/users?userClass=OB&page=0&size=1')
      .set('Authorization', header)
      .expect(200);
    assert.equal(list.body.data.totalElements, 1);
    assert.equal(list.body.data.content[0].id, Number(member.id));
    const leaders = await http().get('/users/leaders').expect(200);
    assert.equal(leaders.body.data.leader.role, 'LEADER');
  });

  it('creates, lists, edits and soft-deletes boards with real BIT and date mappings', async () => {
    const owner = await fixtureUser();
    const other = await fixtureUser(UserRole.MEMBER, '999');
    const header = `Bearer ${tokens.issueAccessToken(owner)}`;
    const dto = { category: 'FREE', title: '첫 글', content: '본문' };
    await http()
      .post('/boards')
      .set('Authorization', header)
      .send(dto)
      .expect(200);
    const board = await db
      .getRepository(Board)
      .findOneByOrFail({ title: dto.title });
    assert.equal(board.deleted, false);
    assert.equal(board.userId, String(owner.id));
    const list = await http()
      .get('/boards?category=FREE&page=0&size=1')
      .expect(200);
    assert.equal(list.body.data.totalElements, 1);
    assert.match(list.body.data.content[0].createdAt, /^\d{2}-\d{2}-\d{2}$/);
    await http()
      .patch(`/boards/${board.id}`)
      .set('Authorization', `Bearer ${tokens.issueAccessToken(other)}`)
      .send({ ...dto, title: '변조' })
      .expect(403);
    await http()
      .patch(`/boards/${board.id}`)
      .set('Authorization', header)
      .send({ ...dto, title: '수정' })
      .expect(200);
    assert.equal(
      (await db.getRepository(Board).findOneByOrFail({ id: board.id })).title,
      '수정',
    );
    await http()
      .delete(`/boards/${board.id}`)
      .set('Authorization', header)
      .expect(200);
    assert.equal(
      (await db.getRepository(Board).findOneByOrFail({ id: board.id })).deleted,
      true,
    );
    const empty = await http().get('/boards').expect(200);
    assert.equal(empty.body.data.totalElements, 0);
    await http().get(`/boards/${board.id}`).expect(404);
  });

  it('persists replies, includes writer details and restricts deletion to the author', async () => {
    const owner = await fixtureUser();
    const other = await fixtureUser(UserRole.MEMBER, '999');
    const header = `Bearer ${tokens.issueAccessToken(owner)}`;
    await http()
      .post('/boards')
      .set('Authorization', header)
      .send({ category: 'NOTICE', title: '공지', content: '공지 내용' })
      .expect(200);
    const board = await db
      .getRepository(Board)
      .findOneByOrFail({ title: '공지' });
    await http()
      .post(`/boards/${board.id}/replies`)
      .set('Authorization', header)
      .send({ content: '댓글' })
      .expect(200);
    const reply = await db
      .getRepository(Reply)
      .findOneByOrFail({ boardId: board.id });
    const detail = await http().get(`/boards/${board.id}`).expect(200);
    assert.equal(detail.body.data.replies.replies[0].content, '댓글');
    assert.match(detail.body.data.createdAt, /^\d{4}년 \d{2}월 \d{2}일/);
    await http()
      .delete(`/replies/${reply.id}`)
      .set('Authorization', `Bearer ${tokens.issueAccessToken(other)}`)
      .expect(403);
    await http()
      .delete(`/replies/${reply.id}`)
      .set('Authorization', header)
      .expect(200);
    assert.equal(await db.getRepository(Reply).count(), 0);
  });

  it('creates, updates, retrieves and deletes templates through admin HTTP endpoints', async () => {
    const admin = await fixtureUser(UserRole.LEADER);
    const header = `Bearer ${tokens.issueAccessToken(admin)}`;
    await http()
      .post('/admin/boards/templates')
      .set('Authorization', header)
      .send({ title: '양식', contents: '내용' })
      .expect(200);
    const template = await db
      .getRepository(BoardTemplate)
      .findOneByOrFail({ title: '양식' });
    await http()
      .patch(`/admin/boards/templates/${template.id}`)
      .set('Authorization', header)
      .send({ title: '새 양식', contents: '새 내용' })
      .expect(200);
    assert.equal(
      (
        await db
          .getRepository(BoardTemplate)
          .findOneByOrFail({ id: template.id })
      ).contents,
      '새 내용',
    );
    const list = await http()
      .get('/admin/boards/templates')
      .set('Authorization', header)
      .expect(200);
    assert.equal(list.body.data.templates.length, 1);
    const detail = await http()
      .get(`/admin/boards/templates/${template.id}`)
      .set('Authorization', header)
      .expect(200);
    assert.equal(detail.body.data.contents, '새 내용');
    await http()
      .delete(`/admin/boards/templates/${template.id}`)
      .set('Authorization', header)
      .expect(200);
    assert.equal(await db.getRepository(BoardTemplate).count(), 0);
  });

  it('rejects invalid request bodies and paging without persisting partial data', async () => {
    const owner = await fixtureUser();
    await http()
      .post('/boards')
      .set('Authorization', `Bearer ${tokens.issueAccessToken(owner)}`)
      .send({ category: 'INVALID', title: '', content: 'x', userId: 999 })
      .expect(400);
    await http().get('/boards?size=100000').expect(400);
    await http().get('/boards/not-an-id').expect(400);
    assert.equal(await db.getRepository(Board).count(), 0);
  });

  it('applies all nine original migrations and responds to a database health check', async () => {
    const history: { version: string; success: number }[] = await db.query(
      'SELECT version, success FROM flyway_schema_history ORDER BY installed_rank',
    );
    assert.deepEqual(
      history.map((row) => row.version),
      ['1', '2', '3', '4', '5', '6', '7', '8', '9'],
    );
    assert.ok(history.every((row) => row.success === 1));
    assert.ok(app);
    await request(app.getHttpServer())
      .get('/healthcheck')
      .expect(200)
      .expect('ok');
  });

  it('persists recruitment changes and selects the latest semester with inclusive dates', async () => {
    const admin = await fixtureUser(UserRole.LEADER);
    const header = `Bearer ${tokens.issueAccessToken(admin)}`;
    await http()
      .post('/admin/recruit')
      .set('Authorization', header)
      .send(recruitmentDto)
      .expect(200);
    const recruitment = await db
      .getRepository(Recruitment)
      .findOneByOrFail({ semester: 40 });
    for (const key of Object.keys(recruitmentDto) as Array<
      keyof typeof recruitmentDto
    >) {
      assert.equal(recruitment[key], recruitmentDto[key]);
    }
    await http()
      .post('/admin/recruit')
      .set('Authorization', header)
      .send({ ...recruitmentDto, semester: 39 })
      .expect(200);
    const latest = await http().get('/application/recruit').expect(200);
    assert.equal(latest.body.data.semester, 40);
    assert.equal(latest.body.data.startDate, '09월01일');
    const service = app?.get(RecruitmentFinder);
    assert.ok(service);
    assert.equal(await service.availability('2026-09-01'), true);
    assert.equal(await service.availability('2026-09-30'), true);
    assert.equal(await service.availability('2026-10-01'), false);
    await http().get('/application/availability').expect(200);
    await http().get('/admin/recruit').set('Authorization', header).expect(200);
    const all = await http()
      .get('/admin/recruits')
      .set('Authorization', header)
      .expect(200);
    assert.deepEqual(
      all.body.data.map((period: { semester: number }) => period.semester),
      [40, 39],
    );
    await http()
      .patch(`/admin/recruit/${recruitment.id}`)
      .set('Authorization', header)
      .send({ ...recruitmentDto, endDate: '2026-09-25' })
      .expect(200);
    assert.equal(
      (
        await db
          .getRepository(Recruitment)
          .findOneByOrFail({ id: recruitment.id })
      ).endDate,
      '2026-09-25',
    );
    await http()
      .delete(`/admin/recruit/${recruitment.id}`)
      .set('Authorization', header)
      .expect(200);
    assert.equal(
      await db.getRepository(Recruitment).countBy({ id: recruitment.id }),
      0,
    );
  });

  it('stores interview times in Seoul time and enforces MySQL foreign keys on deletion', async () => {
    const admin = await fixtureUser(UserRole.LEADER);
    const header = `Bearer ${tokens.issueAccessToken(admin)}`;
    const recruitment = await fixtureRecruitment();
    await http()
      .post(`/admin/recruit/${recruitment.id}/interview-times`)
      .set('Authorization', header)
      .send({ interviewDateTime: '2026-10-02T14:30:00' })
      .expect(200);
    const time = await db
      .getRepository(InterviewTime)
      .findOneByOrFail({ recruitmentPeriodId: recruitment.id });
    assert.equal(
      time.interviewDateTime?.toISOString(),
      '2026-10-02T05:30:00.000Z',
    );
    const list = await http()
      .get(`/application/recruit/${recruitment.id}/interview-times`)
      .expect(200);
    assert.equal(list.body.data[0].interviewDateTime, '2026-10-02 14:30');
    await http()
      .get(`/admin/recruit/${recruitment.id}/interview-times`)
      .set('Authorization', header)
      .expect(200);
    await http()
      .delete(`/admin/recruit/${recruitment.id}`)
      .set('Authorization', header)
      .expect(409);
    await http()
      .patch(`/admin/interview-times/${time.id}`)
      .set('Authorization', header)
      .send({ interviewDateTime: '2026-10-02T15:00:00' })
      .expect(200);
    assert.equal(
      (
        await db.getRepository(InterviewTime).findOneByOrFail({ id: time.id })
      ).interviewDateTime?.toISOString(),
      '2026-10-02T06:00:00.000Z',
    );
    await http()
      .delete(`/admin/interview-times/${time.id}`)
      .set('Authorization', header)
      .expect(200);
    assert.equal(await db.getRepository(InterviewTime).count(), 0);
  });

  it('atomically persists applications, club activities and interview selections and sends a receipt over SMTP', async () => {
    const recruitment = await fixtureRecruitment();
    const time = await db.getRepository(InterviewTime).save({
      recruitmentPeriodId: recruitment.id,
      interviewDateTime: new Date('2026-10-02T05:30:00Z'),
    });
    await http()
      .post('/application')
      .send({ ...applicationDto, availableInterviewTimeIds: [Number(time.id)] })
      .expect(200);
    const application = await db
      .getRepository(Application)
      .findOneByOrFail({ email: applicationDto.email });
    assert.equal(application.studentId, '2023123456');
    assert.equal(application.birthDate, '2003-01-02');
    assert.equal(application.documentPass, null);
    assert.equal(
      await db
        .getRepository(ClubActivity)
        .countBy({ applicationId: application.id }),
      1,
    );
    assert.equal(
      await db
        .getRepository(ApplicationInterviewTime)
        .countBy({ applicationId: application.id, interviewTimeId: time.id }),
      1,
    );
    assert.equal(
      await db
        .getRepository(ApplicationResultLog)
        .countBy({ applicationId: application.id }),
      1,
    );
    assert.equal(external.messages.length, 1);
    assert.match(external.messages[0] ?? '', /applicant@example.test/);
    const admin = await fixtureUser(UserRole.LEADER);
    const detail = await http()
      .get(`/admin/forms/${application.id}`)
      .set('Authorization', `Bearer ${tokens.issueAccessToken(admin)}`)
      .expect(200);
    assert.equal(detail.body.data.activities[0].clubName, '개발동아리');
    assert.equal(detail.body.data.activities[0].startDate, '2025-03-01');
    assert.equal(detail.body.data.activities[0].endDate, '2025-12-31');
    assert.equal(
      detail.body.data.availableInterviewTimes[0].interviewDateTime,
      '2026-10-02 14:30',
    );
    assert.match(
      detail.body.data.photo,
      /\/test-bucket\/store-image\/test-photo.jpg$/,
    );
    assert.equal(detail.body.data.photoKey, undefined);
  });

  it('rolls back already inserted application and activity rows when an interview is invalid', async () => {
    await http()
      .post('/application')
      .send({ ...applicationDto, availableInterviewTimeIds: [999999] })
      .expect(400);
    assert.equal(await db.getRepository(Application).count(), 0);
    assert.equal(await db.getRepository(ClubActivity).count(), 0);
    assert.equal(await db.getRepository(ApplicationInterviewTime).count(), 0);
    assert.equal(await db.getRepository(ApplicationResultLog).count(), 0);
    assert.equal(external.messages.length, 0);
  });

  it('rejects a real interview belonging to another semester and rolls back all writes', async () => {
    const recruitment = await fixtureRecruitment(39);
    const time = await db.getRepository(InterviewTime).save({
      recruitmentPeriodId: recruitment.id,
      interviewDateTime: new Date(),
    });
    await http()
      .post('/application')
      .send({ ...applicationDto, availableInterviewTimeIds: [Number(time.id)] })
      .expect(400);
    assert.equal(await db.getRepository(Application).count(), 0);
    assert.equal(await db.getRepository(ClubActivity).count(), 0);
  });

  it('preserves a committed application when SMTP fails and allows a successful receipt retry', async () => {
    external.rejectMail = true;
    await http().post('/application').send(applicationDto).expect(200);
    const application = await db
      .getRepository(Application)
      .findOneByOrFail({ email: applicationDto.email });
    assert.equal(
      await db
        .getRepository(ClubActivity)
        .countBy({ applicationId: application.id }),
      1,
    );
    assert.equal(await db.getRepository(ApplicationResultLog).count(), 0);
    assert.equal(external.messages.length, 0);
    external.rejectMail = false;
    await app?.get(ApplicationNotifier).notify(application.id, null);
    assert.equal(await db.getRepository(ApplicationResultLog).count(), 1);
    assert.equal(external.messages.length, 1);
  });

  it('persists true/false/null decisions, filters them correctly and updates assigned interviews', async () => {
    await http().post('/application').send(applicationDto).expect(200);
    const application = await db
      .getRepository(Application)
      .findOneByOrFail({ email: applicationDto.email });
    const admin = await fixtureUser(UserRole.LEADER);
    const header = `Bearer ${tokens.issueAccessToken(admin)}`;
    const pending = await http()
      .get('/admin/forms?semester=40')
      .set('Authorization', header)
      .expect(200);
    assert.equal(pending.body.data.content[0].documentPass, null);
    await http()
      .patch(`/admin/forms/${application.id}/pass`)
      .set('Authorization', header)
      .send({ documentPass: true, finalPass: false })
      .expect(200);
    const saved = await db
      .getRepository(Application)
      .findOneByOrFail({ id: application.id });
    assert.equal(saved.documentPass, true);
    assert.equal(saved.finalPass, false);
    const selected = await http()
      .get(
        '/admin/forms?semester=40&documentPass=true&finalPass=false&page=0&size=1',
      )
      .set('Authorization', header)
      .expect(200);
    assert.equal(selected.body.data.totalElements, 1);
    const noMatch = await http()
      .get('/admin/forms?semester=40&documentPass=false&finalPass=false')
      .set('Authorization', header)
      .expect(200);
    assert.equal(noMatch.body.data.totalElements, 0);
    await http()
      .patch(`/admin/forms/${application.id}/interviewTime`)
      .set('Authorization', header)
      .send({ time: '2026-10-02T14:30:00' })
      .expect(200);
    assert.equal(
      (
        await db
          .getRepository(Application)
          .findOneByOrFail({ id: application.id })
      ).interviewTime?.toISOString(),
      '2026-10-02T05:30:00.000Z',
    );
    await http()
      .patch(`/admin/forms/${application.id}/pass`)
      .set('Authorization', header)
      .send({ documentPass: null, finalPass: null })
      .expect(200);
    assert.equal(
      (
        await db
          .getRepository(Application)
          .findOneByOrFail({ id: application.id })
      ).documentPass,
      null,
    );
    await http()
      .get('/admin/forms?semester=40&documentPass=garbage')
      .set('Authorization', header)
      .expect(400);
  });

  it('records result notifications after SMTP success and serializes concurrent duplicate requests', async () => {
    await http().post('/application').send(applicationDto).expect(200);
    const application = await db
      .getRepository(Application)
      .findOneByOrFail({ email: applicationDto.email });
    await db
      .getRepository(Application)
      .update(application.id, { documentPass: true, finalPass: null });
    const admin = await fixtureUser(UserRole.LEADER);
    const header = `Bearer ${tokens.issueAccessToken(admin)}`;
    await Promise.all([
      http()
        .post('/admin/forms/results')
        .set('Authorization', header)
        .send({ documentPass: true, finalPass: null })
        .expect(200),
      http()
        .post('/admin/forms/results')
        .set('Authorization', header)
        .send({ documentPass: true, finalPass: null })
        .expect(200),
    ]);
    assert.equal(external.messages.length, 2);
    assert.equal(
      await db.getRepository(ApplicationResultLog).countBy({
        applicationId: application.id,
        notificationType: NotificationType.DOCUMENT_PASS,
      }),
      1,
    );
    await db
      .getRepository(Application)
      .update(application.id, { finalPass: true });
    external.rejectMail = true;
    await http()
      .post('/admin/forms/results')
      .set('Authorization', header)
      .send({ documentPass: true, finalPass: true })
      .expect(502);
    assert.equal(
      await db
        .getRepository(ApplicationResultLog)
        .countBy({ notificationType: NotificationType.FINAL_PASS }),
      0,
    );
    external.rejectMail = false;
    await http()
      .post('/admin/forms/results')
      .set('Authorization', header)
      .send({ documentPass: true, finalPass: true })
      .expect(200);
    assert.equal(
      await db
        .getRepository(ApplicationResultLog)
        .countBy({ notificationType: NotificationType.FINAL_PASS }),
      1,
    );
  });

  it('stores recruitment email subscriptions, sends through SMTP and leaves failed deliveries retryable', async () => {
    const admin = await fixtureUser(UserRole.LEADER);
    const header = `Bearer ${tokens.issueAccessToken(admin)}`;
    await http()
      .post('/application/emailAlarm')
      .send({ email: 'waiting@example.test', semester: 40 })
      .expect(200);
    const alarm = await db
      .getRepository(EmailAlarm)
      .findOneByOrFail({ email: 'waiting@example.test' });
    const waiting = await http()
      .get('/admin/email/apply-start-email?semester=40')
      .set('Authorization', header)
      .expect(200);
    assert.equal(waiting.body.data.emailAlarms[0].sentAt, null);
    external.rejectMail = true;
    await http()
      .post('/admin/email/apply-start-email')
      .set('Authorization', header)
      .expect(502);
    assert.equal(
      (await db.getRepository(EmailAlarm).findOneByOrFail({ id: alarm.id }))
        .sentAt,
      null,
    );
    external.rejectMail = false;
    await http()
      .post('/admin/email/apply-start-email')
      .set('Authorization', header)
      .expect(200);
    await http()
      .post('/admin/email/apply-start-email')
      .set('Authorization', header)
      .expect(200);
    assert.ok(
      (await db.getRepository(EmailAlarm).findOneByOrFail({ id: alarm.id }))
        .sentAt,
    );
    assert.equal(external.messages.length, 1);
    await http()
      .post('/application/email-confirmation')
      .send({ email: 'verify@example.test' })
      .expect(200);
    assert.equal(external.messages.length, 2);
    await http()
      .post('/application/email-confirmation')
      .send({ email: 'invalid' })
      .expect(400);
  });

  it('accepts recruitment email subscriptions without an assigned semester', async () => {
    const admin = await fixtureUser(UserRole.LEADER);
    // Clients subscribe with semester 0 before a recruitment is announced; the Vue client sends it as a string.
    await http()
      .post('/application/emailAlarm')
      .send({ email: 'unassigned@example.test', semester: '0' })
      .expect(200);
    const waiting = await http()
      .get('/admin/email/apply-start-email?semester=0')
      .set('Authorization', `Bearer ${tokens.issueAccessToken(admin)}`)
      .expect(200);
    assert.deepEqual(
      waiting.body.data.emailAlarms.map(
        (alarm: { email: string; semester: number }) => [
          alarm.email,
          alarm.semester,
        ],
      ),
      [['unassigned@example.test', 0]],
    );
    await http()
      .post('/application/emailAlarm')
      .send({ email: 'negative@example.test', semester: -1 })
      .expect(400);
  });

  it('logs one line per request with its ID, caller and failure reason', async () => {
    const admin = await fixtureUser(UserRole.LEADER);
    logs.entries.length = 0;
    const health = await http().get('/healthcheck').expect(200);
    assert.match(String(health.headers['x-request-id']), /^[0-9a-f-]{36}$/);
    await http()
      .post('/application/emailAlarm')
      .set('X-Request-Id', 'test-request-1')
      .send({ email: 'not-an-email', semester: 0 })
      .expect('X-Request-Id', 'test-request-1')
      .expect(400);
    await http()
      .get('/admin/users')
      .set('Authorization', `Bearer ${tokens.issueAccessToken(admin)}`)
      .expect(200);
    await http()
      .get('/admin/users?page=0&token=secret-value')
      .set('Authorization', `Bearer ${tokens.issueAccessToken(admin)}`)
      .expect(400);
    await http()
      .get('/admin/users')
      .set('Authorization', 'Bearer not-a-jwt')
      .expect(401);
    await http()
      .post('/oauth/kakao')
      .send({ kakaoCode: 'invalid' })
      .expect(401);

    const requests = logs.entries.filter((entry) => entry.context === 'HTTP');
    const find = (predicate: (fields: Record<string, unknown>) => boolean) => {
      const entry = requests.find((item) => predicate(item.fields));
      assert.ok(entry, 'request log entry');
      return entry;
    };
    // Successful health checks run every few seconds and are not logged.
    assert.equal(
      requests.some((entry) => entry.fields.path === '/healthcheck'),
      false,
    );
    const invalid = find((fields) => fields.requestId === 'test-request-1');
    assert.equal(invalid.level, 'warn');
    assert.equal(
      invalid.message,
      `POST /application/emailAlarm 400 ${invalid.fields.durationMs}ms`,
    );
    assert.equal(invalid.fields.route, '/application/emailAlarm');
    assert.match(String(invalid.fields.error), /email must be an email/);
    const listed = find(
      (fields) => fields.path === '/admin/users' && fields.status === 200,
    );
    assert.equal(listed.level, 'log');
    assert.equal(listed.fields.userId, String(admin.id));
    const redacted = find(
      (fields) => fields.status === 400 && fields.path === '/admin/users',
    );
    assert.equal(redacted.fields.query, 'page=0&token=%5Bredacted%5D');
    const rejected = find(
      (fields) => fields.path === '/admin/users' && fields.status === 401,
    );
    assert.match(String(rejected.fields.cause), /jwt malformed/);
    const kakao = find((fields) => fields.path === '/oauth/kakao');
    assert.equal(kakao.fields.cause, 'Kakao token 400: invalid_grant');
    // Bodies and credentials never reach the log.
    const logged = JSON.stringify(logs.entries);
    assert.equal(logged.includes('not-an-email'), false);
    assert.equal(logged.includes('secret-value'), false);
    assert.equal(logged.includes(tokens.issueAccessToken(admin)), false);
  });

  it('uploads real bytes to MinIO using the issued presigned URL', async () => {
    const bytes = Buffer.from('test-image-bytes');
    const result = await http()
      .post('/apply/forms/image/presigned-url')
      .send({
        fileName: 'test.png',
        contentType: 'image/png',
        fileSize: bytes.length,
      })
      .expect(200);
    const { uploadUrl, imageKey, uploadHeaders } = result.body.data;
    const upload = await fetch(uploadUrl, {
      method: 'PUT',
      headers: uploadHeaders,
      body: bytes,
    });
    assert.equal(upload.status, 200, await upload.text());
    assert.ok(storage);
    const object = await storage.send(
      new GetObjectCommand({ Bucket: 'test-bucket', Key: imageKey }),
    );
    assert.equal(await object.Body?.transformToString(), bytes.toString());
    assert.equal(object.ContentType, 'image/png');
    assert.equal(new URL(uploadUrl).searchParams.get('X-Amz-Expires'), '300');
    assert.equal(uploadHeaders['x-amz-acl'], 'public-read');
    await http()
      .post('/apply/forms/image/presigned-url')
      .send({ fileName: 'bad.svg', contentType: 'image/svg+xml', fileSize: 10 })
      .expect(400);
    await http()
      .post('/apply/forms/image/presigned-url')
      .send({
        fileName: 'big.png',
        contentType: 'image/png',
        fileSize: 10485761,
      })
      .expect(400);
  });

  it('preserves legacy image URLs and lossless BIGINT response values', async () => {
    await http().post('/application').send(applicationDto).expect(200);
    const application = await db
      .getRepository(Application)
      .findOneByOrFail({ email: applicationDto.email });
    await db.getRepository(Application).update(application.id, {
      photoKey: null,
      photo: 'https://old.example.test/photo.jpg',
      studentId: '9007199254740993',
    });
    const admin = await fixtureUser(UserRole.LEADER);
    const detail = await http()
      .get(`/admin/forms/${application.id}`)
      .set('Authorization', `Bearer ${tokens.issueAccessToken(admin)}`)
      .expect(200);
    assert.equal(detail.body.data.photo, 'https://old.example.test/photo.jpg');
    assert.equal(detail.body.data.studentId, '9007199254740993');
    assert.ok(app);
    const settings = app.get(Settings);
    const awsSettings = new Settings(
      settings.database,
      settings.jwtSecret,
      settings.port,
      'aws',
      settings.origins,
      settings.kakao,
      settings.mail,
      {
        ...settings.storage,
        provider: 's3',
        publicUrl: 'https://images.example.test',
      },
    );
    const images = new S3ImageStorage(awsSettings);
    assert.equal(
      images.publicUrl('store-image/new.jpg'),
      'https://images.example.test/store-image/new.jpg',
    );
    images.onModuleDestroy();
  });

  it('selects MinIO storage independently of APP_PROFILE', async () => {
    const base = {
      DATABASE_URL: 'mysql://127.0.0.1:3306/yg',
      JWT_SECRET_KEY: Buffer.alloc(32, 't').toString('base64'),
      KAKAO_CLIENT_ID: 'test',
      KAKAO_CLIENT_SECRET: 'test',
      AWS_S3_BUCKET: 'yg-img-storage',
      AWS_S3_PUBLIC_URL: 'https://minio.example.test',
    };
    const minioEnv = {
      S3_ENDPOINT: 'https://minio.example.test',
      AWS_ACCESS_KEY_ID: 'yg-server',
      AWS_SECRET_ACCESS_KEY: 'secret',
    };
    assert.equal(
      loadSettings({ ...base, ...minioEnv, APP_PROFILE: 'home' }).storage
        .provider,
      'minio',
    );
    assert.equal(
      loadSettings({ ...base, APP_PROFILE: 'aws' }).storage.provider,
      's3',
    );
    const settings = loadSettings({
      ...base,
      ...minioEnv,
      APP_PROFILE: 'aws',
      STORAGE_PROVIDER: 'minio',
    });
    assert.equal(settings.profile, 'aws');
    assert.equal(settings.storage.provider, 'minio');
    const images = new S3ImageStorage(settings);
    const { uploadUrl, uploadHeaders } = await images.presignUpload({
      key: 'store-image/test.png',
      contentType: 'image/png',
      contentLength: 10,
    });
    assert.ok(
      uploadUrl.startsWith('https://minio.example.test/yg-img-storage/'),
    );
    assert.equal(uploadHeaders['x-amz-acl'], 'public-read');
    assert.equal(
      images.publicUrl('store-image/new.jpg'),
      'https://minio.example.test/yg-img-storage/store-image/new.jpg',
    );
    images.onModuleDestroy();
    assert.throws(
      () =>
        loadSettings({
          ...base,
          APP_PROFILE: 'aws',
          STORAGE_PROVIDER: 'minio',
        }),
      /MinIO storage requires S3_ENDPOINT/,
    );
    assert.throws(
      () => loadSettings({ ...base, ...minioEnv, STORAGE_PROVIDER: 'gcs' }),
      /STORAGE_PROVIDER must be minio or s3/,
    );
  });

  it('persists and reloads a user through a real TypeORM repository', async () => {
    const repository = db.getRepository(User);
    const saved = await repository.save(
      repository.create({
        kakaoId: '12345',
        name: '테스트',
        phoneNumber: '010-1234-5678',
        studentId: 20,
        major: '컴퓨터과학',
        semester: 40,
        role: UserRole.MEMBER,
        userClass: UserClass.NONE,
      }),
    );
    const reloaded = await repository.findOneByOrFail({ id: saved.id });
    assert.equal(reloaded.kakaoId, '12345');
    assert.equal(reloaded.name, '테스트');
    await repository.delete(saved.id);
    assert.equal(await repository.count(), 0);
  });
});
