import { Column, Entity, PrimaryColumn } from 'typeorm';
import { BaseEntity, bitBoolean } from '../database/base.entity';

@Entity('recruitment_period')
export class Recruitment extends BaseEntity {
  @Column({ type: 'int', nullable: true }) semester!: number | null;
  @Column({ name: 'start_date', type: 'date' }) startDate!: string;
  @Column({ name: 'end_date', type: 'date' }) endDate!: string;
  @Column({ name: 'first_result_date', type: 'date' }) firstResultDate!: string;
  @Column({ name: 'interview_start_date', type: 'date' })
  interviewStartDate!: string;
  @Column({ name: 'interview_end_date', type: 'date' })
  interviewEndDate!: string;
  @Column({ name: 'final_result_date', type: 'date' }) finalResultDate!: string;
  @Column({ name: 'orientation_date', type: 'date' }) orientationDate!: string;
}

@Entity('interview_time')
export class InterviewTime extends BaseEntity {
  @Column({ name: 'recruitment_period_id', type: 'bigint', nullable: true })
  recruitmentPeriodId!: string;
  @Column({ name: 'interview_date_time', type: 'datetime', nullable: true })
  interviewDateTime!: Date | null;
}

@Entity('application')
export class Application extends BaseEntity {
  @Column({ type: 'varchar', length: 255 }) name!: string;
  @Column({ type: 'varchar', length: 3000, nullable: true }) photo!:
    | string
    | null;
  @Column({ name: 'photo_key', type: 'varchar', length: 1024, nullable: true })
  photoKey!: string | null;
  @Column({ name: 'birth_date', type: 'date', nullable: true }) birthDate!:
    | string
    | null;
  @Column({ name: 'student_id', type: 'bigint' }) studentId!: string;
  @Column({ type: 'varchar', length: 255, nullable: true }) email!:
    | string
    | null;
  @Column({ type: 'varchar', length: 25 }) major!: string;
  @Column({ name: 'phone_number', type: 'varchar', length: 25 })
  phoneNumber!: string;
  @Column({ name: 'self_introduction', type: 'text' })
  selfIntroduction!: string;
  @Column({ name: 'apply_reason', type: 'text' }) applyReason!: string;
  @Column({ name: 'skill_evaluation', type: 'text' }) skillEvaluation!: string;
  @Column({ name: 'golf_memory', type: 'text' }) golfMemory!: string;
  @Column({ name: 'swing_video', type: 'varchar', length: 3000 })
  swingVideo!: string;
  @Column({ name: 'submit_time', type: 'datetime' }) submitTime!: Date;
  @Column({
    name: 'document_pass',
    type: 'bit',
    nullable: true,
    transformer: bitBoolean,
  })
  documentPass!: boolean | null;
  @Column({
    name: 'final_pass',
    type: 'bit',
    nullable: true,
    transformer: bitBoolean,
  })
  finalPass!: boolean | null;
  @Column({ name: 'interview_time', type: 'datetime', nullable: true })
  interviewTime!: Date | null;
  @Column({ type: 'bigint', nullable: true }) semester!: string | null;
}

@Entity('club_activity')
export class ClubActivity extends BaseEntity {
  @Column({ name: 'application_id', type: 'bigint', nullable: true })
  applicationId!: string;
  @Column({ name: 'club_name', type: 'varchar', length: 60, nullable: true })
  clubName!: string;
  @Column({ name: 'start_date', type: 'date', nullable: true }) startDate!:
    | string
    | null;
  @Column({ name: 'end_date', type: 'date', nullable: true }) endDate!:
    | string
    | null;
  @Column({ type: 'varchar', length: 1000, nullable: true }) role!:
    | string
    | null;
}

@Entity('application_available_interview_time')
export class ApplicationInterviewTime {
  @PrimaryColumn({ name: 'application_id', type: 'bigint' })
  applicationId!: string;
  @PrimaryColumn({ name: 'interview_time_id', type: 'bigint' })
  interviewTimeId!: string;
}

@Entity('email_alarm')
export class EmailAlarm extends BaseEntity {
  @Column({ type: 'varchar', length: 255 }) email!: string;
  @Column({ type: 'bigint', nullable: true }) semester!: string | null;
  @Column({ name: 'sent_at', type: 'datetime', precision: 6, nullable: true })
  sentAt!: Date | null;
}

export enum NotificationType {
  DOCUMENT_PASS = 'DOCUMENT_PASS',
  FINAL_PASS = 'FINAL_PASS',
  FAIL = 'FAIL',
}

@Entity('application_result_log')
export class ApplicationResultLog extends BaseEntity {
  @Column({ name: 'application_id', type: 'bigint', nullable: true })
  applicationId!: string;
  @Column({
    name: 'notification_type',
    type: 'varchar',
    length: 64,
    nullable: true,
  })
  notificationType!: NotificationType | null;
  @Column({ name: 'sent_at', type: 'datetime', precision: 6, nullable: true })
  sentAt!: Date | null;
}
