import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { EmailService } from '../email/email.service';
import { ImageService } from '../storage/image.service';
import { InterviewTime, Recruitment } from './application.entity';
import { ApplicationsController } from './applications.controller';
import { ApplicationsService } from './applications.service';
import { RecruitmentService } from './recruitment.service';

@Module({
  imports: [TypeOrmModule.forFeature([Recruitment, InterviewTime])],
  controllers: [ApplicationsController],
  providers: [
    ApplicationsService,
    RecruitmentService,
    EmailService,
    ImageService,
  ],
})
export class ApplicationsModule {}
