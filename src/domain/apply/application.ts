import { Column, Entity, PrimaryColumn } from 'typeorm';
import { InvalidInputError } from '../../support/errors';
import { BaseEntity, bitBoolean } from '../common/base-entity';

/** What an applicant submits. Calendar dates are `YYYY-MM-DD` strings. */
export interface ApplicationForm {
  name: string;
  photoKey: string | null;
  birthDate: string | null;
  studentId: number;
  email: string;
  major: string;
  phoneNumber: string;
  selfIntroduction: string;
  applyReason: string;
  skillEvaluation: string;
  golfMemory: string;
  swingVideo: string;
  semester: number;
}

export interface ClubActivityRecord {
  clubName: string;
  startDate: string | null;
  endDate: string | null;
  role: string | null;
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

  /** The server clock sets the submit time; decisions start undecided (null). */
  static submit(form: ApplicationForm, now: Date): Application {
    const application = new Application();
    application.name = form.name;
    application.photoKey = form.photoKey;
    application.birthDate = form.birthDate;
    application.studentId = String(form.studentId);
    application.email = form.email;
    application.major = form.major;
    application.phoneNumber = form.phoneNumber;
    application.selfIntroduction = form.selfIntroduction;
    application.applyReason = form.applyReason;
    application.skillEvaluation = form.skillEvaluation;
    application.golfMemory = form.golfMemory;
    application.swingVideo = form.swingVideo;
    application.semester = String(form.semester);
    application.submitTime = now;
    application.documentPass = null;
    application.finalPass = null;
    application.interviewTime = null;
    application.photo = null;
    return application;
  }
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

  static validate(activity: ClubActivityRecord): void {
    if (
      activity.startDate &&
      activity.endDate &&
      activity.startDate > activity.endDate
    )
      throw new InvalidInputError(
        '동아리 활동 시작일은 종료일보다 늦을 수 없습니다.',
      );
  }

  static record(
    activity: ClubActivityRecord,
    applicationId: string,
  ): ClubActivity {
    ClubActivity.validate(activity);
    const record = new ClubActivity();
    record.applicationId = applicationId;
    record.clubName = activity.clubName;
    record.startDate = activity.startDate;
    record.endDate = activity.endDate;
    record.role = activity.role;
    return record;
  }
}

@Entity('application_available_interview_time')
export class ApplicationInterviewTime {
  @PrimaryColumn({ name: 'application_id', type: 'bigint' })
  applicationId!: string;
  @PrimaryColumn({ name: 'interview_time_id', type: 'bigint' })
  interviewTimeId!: string;

  static link(
    applicationId: string,
    interviewTimeId: string,
  ): ApplicationInterviewTime {
    const link = new ApplicationInterviewTime();
    link.applicationId = applicationId;
    link.interviewTimeId = interviewTimeId;
    return link;
  }
}
