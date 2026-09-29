import 'reflect-metadata';
import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { Application, ClubActivity } from '../src/domain/apply/application';
import {
  NotificationType,
  notificationTypeOf,
} from '../src/domain/apply/application-result-log';
import { EmailAlarm } from '../src/domain/recruitment/email-alarm';
import { Recruitment } from '../src/domain/recruitment/recruitment';
import { User, UserClass, UserRole } from '../src/domain/user/user';
import { ConflictError, InvalidInputError } from '../src/support/errors';

// Domain rules run without Nest or a database.

const profile = {
  name: '홍길동',
  phoneNumber: '010-0000-0000',
  studentId: 24,
  major: '경영학',
  semester: 40,
};

describe('User', () => {
  it('registers a new member without a role or class', () => {
    const user = User.register(profile, '45678');
    assert.equal(user.kakaoId, '45678');
    assert.equal(user.role, UserRole.MEMBER);
    assert.equal(user.userClass, UserClass.NONE);
    assert.equal(user.hasMemberStatus(), false);
  });
  it('links an unlinked or same Kakao account and keeps role and class', () => {
    const user = Object.assign(User.register(profile, '0'), {
      role: UserRole.LEADER,
      userClass: UserClass.OB,
    });
    user.linkKakao({ ...profile, phoneNumber: '010-9999-9999' }, '999');
    assert.equal(user.kakaoId, '999');
    assert.equal(user.phoneNumber, '010-9999-9999');
    assert.equal(user.role, UserRole.LEADER);
    assert.equal(user.userClass, UserClass.OB);
    user.linkKakao(profile, '999');
    assert.throws(() => user.linkKakao(profile, '998'), ConflictError);
  });
  it('derives admin, blocked and member states from role and class', () => {
    const user = User.register(profile, '1');
    assert.equal(user.isAdmin(), false);
    user.role = UserRole.OB_ASSISTANT_LEADER;
    assert.equal(user.isAdmin(), true);
    user.changeClass(UserClass.BLACK_LIST);
    assert.equal(user.isBlocked(), true);
    assert.equal(user.hasMemberStatus(), true);
  });
});

describe('Recruitment', () => {
  const schedule = {
    semester: 40,
    startDate: '2026-09-01',
    endDate: '2026-09-30',
    firstResultDate: '2026-10-01',
    interviewStartDate: '2026-10-02',
    interviewEndDate: '2026-10-04',
    finalResultDate: '2026-10-05',
    orientationDate: '2026-10-10',
  };
  it('rejects a period or interview window that ends before it starts', () => {
    assert.throws(
      () => Recruitment.open({ ...schedule, endDate: '2026-08-31' }),
      InvalidInputError,
    );
    assert.throws(
      () => Recruitment.open({ ...schedule, interviewEndDate: '2026-10-01' }),
      InvalidInputError,
    );
  });
  it('is open on both the start and the end date', () => {
    const recruitment = Recruitment.open(schedule);
    assert.equal(recruitment.isOpenOn('2026-08-31'), false);
    assert.equal(recruitment.isOpenOn('2026-09-01'), true);
    assert.equal(recruitment.isOpenOn('2026-09-30'), true);
    assert.equal(recruitment.isOpenOn('2026-10-01'), false);
  });
  it('keeps an alarm for an unassigned semester and marks it sent', () => {
    const alarm = EmailAlarm.subscribe('a@example.test', 0);
    assert.equal(alarm.semester, '0');
    assert.equal(alarm.isSent(), false);
    alarm.markSent(new Date());
    assert.equal(alarm.isSent(), true);
  });
});

describe('Application', () => {
  it('starts undecided with the server submit time', () => {
    const now = new Date('2026-09-10T00:00:00Z');
    const application = Application.submit(
      {
        ...profile,
        studentId: 2023123456,
        photoKey: null,
        birthDate: null,
        email: 'a@example.test',
        selfIntroduction: '안녕하세요',
        applyReason: '이유',
        skillEvaluation: '입문',
        golfMemory: '추억',
        swingVideo: '',
      },
      now,
    );
    assert.equal(application.studentId, '2023123456');
    assert.equal(application.semester, '40');
    assert.equal(application.submitTime, now);
    assert.equal(application.documentPass, null);
    assert.equal(application.finalPass, null);
  });
  it('rejects a club activity that ends before it starts', () => {
    const activity = {
      clubName: '동아리',
      startDate: '2025-12-31',
      endDate: '2025-03-01',
      role: null,
    };
    assert.throws(() => ClubActivity.validate(activity), InvalidInputError);
    ClubActivity.validate({ ...activity, endDate: null });
  });
  it('announces a result by the pass decisions', () => {
    assert.equal(
      notificationTypeOf(true, null),
      NotificationType.DOCUMENT_PASS,
    );
    assert.equal(notificationTypeOf(true, true), NotificationType.FINAL_PASS);
    assert.equal(notificationTypeOf(true, false), NotificationType.FAIL);
    assert.equal(notificationTypeOf(false, null), NotificationType.FAIL);
  });
});
