import { Module } from '@nestjs/common';
import { ApplicationNotificationService } from '../../application/apply/application-notification.service';
import { ApplicationPhotoService } from '../../application/apply/application-photo.service';
import { ApplicationQueryService } from '../../application/apply/application-query.service';
import { ApplyService } from '../../application/apply/apply.service';
import { ApplicationFinder } from '../../application/apply/provided/application-finder';
import { ApplicationNotifier } from '../../application/apply/provided/application-notifier';
import { ApplicationPhotoUploader } from '../../application/apply/provided/application-photo-uploader';
import { ApplicationRegister } from '../../application/apply/provided/application-register';
import { ApplicationInterviewTimeRepository } from '../../application/apply/required/application-interview-time-repository';
import { ApplicationRepository } from '../../application/apply/required/application-repository';
import { ApplicationResultLogRepository } from '../../application/apply/required/application-result-log-repository';
import { ClubActivityRepository } from '../../application/apply/required/club-activity-repository';
import { ImageStorage } from '../../application/apply/required/image-storage';
import {
  Application,
  ApplicationInterviewTime,
  ClubActivity,
} from '../../domain/apply/application';
import { ApplicationResultLog } from '../../domain/apply/application-result-log';
import { S3ImageStorage } from '../integration/storage/s3-image-storage';
import { ApplicationController } from '../webapi/apply/application.controller';
import { MailModule } from './mail.module';
import { MailTemplateModule } from './mail-template.module';
import { RecruitmentModule } from './recruitment.module';
import { repositoryProvider } from './repository';

@Module({
  imports: [MailModule, MailTemplateModule, RecruitmentModule],
  controllers: [ApplicationController],
  providers: [
    repositoryProvider(ApplicationRepository, Application),
    repositoryProvider(ClubActivityRepository, ClubActivity),
    repositoryProvider(ApplicationResultLogRepository, ApplicationResultLog),
    repositoryProvider(
      ApplicationInterviewTimeRepository,
      ApplicationInterviewTime,
    ),
    { provide: ImageStorage, useClass: S3ImageStorage },
    ApplicationPhotoService,
    ApplicationQueryService,
    ApplicationNotificationService,
    ApplyService,
    { provide: ApplicationRegister, useExisting: ApplyService },
    { provide: ApplicationFinder, useExisting: ApplicationQueryService },
    {
      provide: ApplicationNotifier,
      useExisting: ApplicationNotificationService,
    },
    { provide: ApplicationPhotoUploader, useExisting: ApplicationPhotoService },
  ],
})
export class ApplyModule {}
