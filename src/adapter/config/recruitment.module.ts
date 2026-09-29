import { Module } from '@nestjs/common';
import { InterviewTimeService } from '../../application/recruitment/interview-time.service';
import { InterviewTimeFinder } from '../../application/recruitment/provided/interview-time-finder';
import { InterviewTimeRegister } from '../../application/recruitment/provided/interview-time-register';
import { RecruitmentAlertManager } from '../../application/recruitment/provided/recruitment-alert-manager';
import { RecruitmentFinder } from '../../application/recruitment/provided/recruitment-finder';
import { RecruitmentRegister } from '../../application/recruitment/provided/recruitment-register';
import { RecruitmentService } from '../../application/recruitment/recruitment.service';
import { RecruitmentAlertService } from '../../application/recruitment/recruitment-alert.service';
import { RecruitmentQueryService } from '../../application/recruitment/recruitment-query.service';
import { EmailAlarmRepository } from '../../application/recruitment/required/email-alarm-repository';
import { InterviewTimeRepository } from '../../application/recruitment/required/interview-time-repository';
import { RecruitmentRepository } from '../../application/recruitment/required/recruitment-repository';
import { EmailAlarm } from '../../domain/recruitment/email-alarm';
import {
  InterviewTime,
  Recruitment,
} from '../../domain/recruitment/recruitment';
import { RecruitmentController } from '../webapi/recruitment/recruitment.controller';
import { RecruitmentAlertController } from '../webapi/recruitment/recruitment-alert.controller';
import { MailModule } from './mail.module';
import { repositoryProvider } from './repository';

@Module({
  imports: [MailModule],
  controllers: [RecruitmentController, RecruitmentAlertController],
  providers: [
    repositoryProvider(RecruitmentRepository, Recruitment),
    repositoryProvider(InterviewTimeRepository, InterviewTime),
    repositoryProvider(EmailAlarmRepository, EmailAlarm),
    RecruitmentQueryService,
    RecruitmentService,
    InterviewTimeService,
    RecruitmentAlertService,
    { provide: RecruitmentFinder, useExisting: RecruitmentQueryService },
    { provide: RecruitmentRegister, useExisting: RecruitmentService },
    { provide: InterviewTimeFinder, useExisting: InterviewTimeService },
    { provide: InterviewTimeRegister, useExisting: InterviewTimeService },
    { provide: RecruitmentAlertManager, useExisting: RecruitmentAlertService },
  ],
  // The apply slice reads interview times only through this port.
  exports: [InterviewTimeFinder],
})
export class RecruitmentModule {}
