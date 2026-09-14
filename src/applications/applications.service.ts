import {
  BadRequestException,
  Injectable,
  Logger,
  NotFoundException,
} from '@nestjs/common';
import { DataSource, In } from 'typeorm';
import { formatDate, localDateTime } from '../common/dates';
import { apiId, pageResponse } from '../common/http';
import { EmailService, notificationType } from '../email/email.service';
import { ImageService } from '../storage/image.service';
import {
  Application,
  ApplicationInterviewTime,
  ClubActivity,
  EmailAlarm,
  InterviewTime,
  Recruitment,
} from './application.entity';
import {
  ApplicationDto,
  EmailAlarmDto,
  FormsQuery,
  PassDto,
  ResultDto,
} from './applications.dto';
import { interviewResponse } from './recruitment.service';

@Injectable()
export class ApplicationsService {
  private readonly logger = new Logger(ApplicationsService.name);
  constructor(
    private readonly db: DataSource,
    private readonly email: EmailService,
    private readonly images: ImageService,
  ) {}

  async apply(dto: ApplicationDto): Promise<void> {
    const saved = await this.db.transaction(async (manager) => {
      const {
        activityClubs,
        availableInterviewTimeIds,
        studentId,
        semester,
        submitTime: _submittedTime,
        ...fields
      } = dto;
      const application = await manager.save(
        Application,
        manager.create(Application, {
          ...fields,
          studentId: String(studentId),
          semester: String(semester),
          submitTime: new Date(),
          documentPass: null,
          finalPass: null,
          interviewTime: null,
          photo: null,
        }),
      );
      if (activityClubs.length) {
        for (const activity of activityClubs)
          if (
            activity.startDate &&
            activity.endDate &&
            activity.startDate > activity.endDate
          )
            throw new BadRequestException(
              '동아리 활동 시작일은 종료일보다 늦을 수 없습니다.',
            );
        await manager.save(
          ClubActivity,
          activityClubs.map((activity) =>
            manager.create(ClubActivity, {
              ...activity,
              applicationId: application.id,
            }),
          ),
        );
      }
      if (availableInterviewTimeIds.length) {
        const ids = availableInterviewTimeIds.map(String);
        const times = await manager.find(InterviewTime, {
          where: { id: In(ids) },
        });
        const periods = await manager.find(Recruitment, {
          where: { id: In(times.map((time) => time.recruitmentPeriodId)) },
        });
        if (
          times.length !== ids.length ||
          periods.some((period) => period.semester !== semester) ||
          !periods.length
        )
          throw new BadRequestException(
            '해당 모집 기수의 면접 시간을 선택해주세요.',
          );
        await manager.save(
          ApplicationInterviewTime,
          ids.map((id) =>
            manager.create(ApplicationInterviewTime, {
              applicationId: application.id,
              interviewTimeId: id,
            }),
          ),
        );
      }
      return application;
    });
    // Receipt email failures must not undo a successfully submitted application.
    try {
      await this.email.applicationNotification(saved.id, null);
    } catch {
      this.logger.warn(`지원서 ${saved.id} 저장 완료, 접수 메일 발송 실패`);
    }
  }
  async emailAlarm(dto: EmailAlarmDto): Promise<void> {
    await this.db
      .getRepository(EmailAlarm)
      .save({ email: dto.email, semester: String(dto.semester), sentAt: null });
  }
  private filtered(documentPass: boolean | null, finalPass: boolean | null) {
    const builder = this.db
      .getRepository(Application)
      .createQueryBuilder('application');
    if (documentPass === null)
      builder.andWhere('application.document_pass IS NULL');
    else
      builder.andWhere('application.document_pass = :documentPass', {
        documentPass: documentPass ? 1 : 0,
      });
    if (finalPass === null) builder.andWhere('application.final_pass IS NULL');
    else
      builder.andWhere('application.final_pass = :finalPass', {
        finalPass: finalPass ? 1 : 0,
      });
    return builder;
  }
  async list(query: FormsQuery) {
    const [applications, total] = await this.filtered(
      query.documentPass,
      query.finalPass,
    )
      .andWhere('application.semester = :semester', {
        semester: query.semester,
      })
      .orderBy('application.interview_time', 'ASC')
      .addOrderBy('application.id', 'ASC')
      .skip(query.page * query.size)
      .take(query.size)
      .getManyAndCount();
    return pageResponse(
      applications.map((application) => ({
        id: apiId(application.id),
        photo: this.images.resolve(application.photoKey, application.photo),
        name: application.name,
        interviewTime: formatDate(application.interviewTime, 'monthTime'),
        documentPass: application.documentPass,
        finalPass: application.finalPass,
      })),
      total,
      query,
    );
  }
  async find(id: string): Promise<Application> {
    const application = await this.db
      .getRepository(Application)
      .findOneBy({ id });
    if (!application)
      throw new NotFoundException('해당 지원서가 존재하지 않습니다.');
    return application;
  }
  async detail(id: string) {
    const application = await this.find(id);
    const activities = await this.db
      .getRepository(ClubActivity)
      .find({ where: { applicationId: id }, order: { id: 'ASC' } });
    const links = await this.db
      .getRepository(ApplicationInterviewTime)
      .findBy({ applicationId: id });
    const times = links.length
      ? await this.db.getRepository(InterviewTime).find({
          where: { id: In(links.map((link) => link.interviewTimeId)) },
          order: { interviewDateTime: 'ASC', id: 'ASC' },
        })
      : [];
    const { photoKey, ...fields } = application;
    return {
      ...fields,
      id: apiId(id),
      studentId: apiId(application.studentId),
      semester:
        application.semester === null ? null : apiId(application.semester),
      photo: this.images.resolve(photoKey, application.photo),
      submitTime: formatDate(application.submitTime, 'monthTime'),
      interviewTime: formatDate(application.interviewTime, 'monthTime'),
      activities: activities.map(({ clubName, startDate, endDate, role }) => ({
        clubName,
        startDate,
        endDate,
        role,
      })),
      availableInterviewTimes: times.map(interviewResponse),
    };
  }
  async updatePass(id: string, dto: PassDto): Promise<void> {
    await this.find(id);
    await this.db.getRepository(Application).update(id, dto);
  }
  async updateInterview(id: string, time: string | null): Promise<void> {
    await this.find(id);
    await this.db.getRepository(Application).update(id, {
      interviewTime: time === null ? null : localDateTime(time),
    });
  }
  async sendResults(dto: ResultDto): Promise<void> {
    const applications = await this.filtered(dto.documentPass, dto.finalPass)
      .orderBy('application.id', 'ASC')
      .getMany();
    const type = notificationType(dto.documentPass, dto.finalPass);
    for (const application of applications)
      await this.email.applicationNotification(application.id, type);
  }
}
