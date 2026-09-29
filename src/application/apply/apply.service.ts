import { Logger } from '@nestjs/common';
import { DataSource } from 'typeorm';
import {
  Application,
  ApplicationInterviewTime,
  ClubActivity,
} from '../../domain/apply/application';
import { describeCause } from '../../support/describe-cause';
import { InvalidInputError } from '../../support/errors';
import { ApplicationService } from '../../support/stereotype';
import { InterviewTimeFinder } from '../recruitment/provided/interview-time-finder';
import { localDateTime } from '../shared/dates';
import { ApplicationNotificationService } from './application-notification.service';
import { ApplicationQueryService } from './application-query.service';
import { ApplicationRegister } from './provided/application-register';
import { ApplicationDto, PassDto } from './provided/apply-requests';
import { ApplicationRepository } from './required/application-repository';

@ApplicationService()
export class ApplyService implements ApplicationRegister {
  private readonly logger = new Logger(ApplyService.name);
  constructor(
    private readonly db: DataSource,
    private readonly applications: ApplicationRepository,
    private readonly interviewTimes: InterviewTimeFinder,
    private readonly query: ApplicationQueryService,
    private readonly notifications: ApplicationNotificationService,
  ) {}

  async apply(request: ApplicationDto): Promise<void> {
    // Everything is checked before the first INSERT, so a rejected form leaves no rows.
    for (const activity of request.activityClubs)
      ClubActivity.validate(activity);
    const interviewTimeIds = request.availableInterviewTimeIds.map(String);
    if (
      interviewTimeIds.length &&
      !(await this.interviewTimes.allBelongToSemester(
        interviewTimeIds,
        request.semester,
      ))
    )
      throw new InvalidInputError('해당 모집 기수의 면접 시간을 선택해주세요.');
    const saved = await this.db.transaction(async (manager) => {
      const application = await manager.save(
        Application,
        Application.submit(request, new Date()),
      );
      if (request.activityClubs.length)
        await manager.save(
          ClubActivity,
          request.activityClubs.map((activity) =>
            ClubActivity.record(activity, application.id),
          ),
        );
      if (interviewTimeIds.length)
        await manager.save(
          ApplicationInterviewTime,
          interviewTimeIds.map((id) =>
            ApplicationInterviewTime.link(application.id, id),
          ),
        );
      return application;
    });
    // Receipt email failures must not undo a successfully submitted application.
    try {
      await this.notifications.notify(saved.id, null);
    } catch (error) {
      this.logger.warn(`지원서 ${saved.id} 저장 완료, 접수 메일 발송 실패`, {
        applicationId: saved.id,
        cause: describeCause(error),
      });
    }
  }
  async updatePass(id: string, request: PassDto): Promise<void> {
    await this.query.getById(id);
    await this.applications.update(id, {
      documentPass: request.documentPass,
      finalPass: request.finalPass,
    });
  }
  async updateInterview(id: string, time: string | null): Promise<void> {
    await this.query.getById(id);
    await this.applications.update(id, {
      interviewTime: time === null ? null : localDateTime(time),
    });
  }
}
