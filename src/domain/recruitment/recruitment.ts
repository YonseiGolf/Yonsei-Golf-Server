import { Column, Entity } from 'typeorm';
import { InvalidInputError } from '../../support/errors';
import { BaseEntity } from '../common/base-entity';

/** Calendar dates are `YYYY-MM-DD` strings in Korean time. */
export interface RecruitmentSchedule {
  semester: number;
  startDate: string;
  endDate: string;
  firstResultDate: string;
  interviewStartDate: string;
  interviewEndDate: string;
  finalResultDate: string;
  orientationDate: string;
}

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

  static open(schedule: RecruitmentSchedule): Recruitment {
    const recruitment = new Recruitment();
    recruitment.reschedule(schedule);
    return recruitment;
  }

  reschedule(schedule: RecruitmentSchedule): void {
    if (
      schedule.startDate > schedule.endDate ||
      schedule.interviewStartDate > schedule.interviewEndDate
    )
      throw new InvalidInputError('시작일은 종료일보다 늦을 수 없습니다.');
    this.semester = schedule.semester;
    this.startDate = schedule.startDate;
    this.endDate = schedule.endDate;
    this.firstResultDate = schedule.firstResultDate;
    this.interviewStartDate = schedule.interviewStartDate;
    this.interviewEndDate = schedule.interviewEndDate;
    this.finalResultDate = schedule.finalResultDate;
    this.orientationDate = schedule.orientationDate;
  }

  /** Both the start and end dates are included. */
  isOpenOn(date: string): boolean {
    return date >= this.startDate && date <= this.endDate;
  }
}

@Entity('interview_time')
export class InterviewTime extends BaseEntity {
  @Column({ name: 'recruitment_period_id', type: 'bigint', nullable: true })
  recruitmentPeriodId!: string;
  @Column({ name: 'interview_date_time', type: 'datetime', nullable: true })
  interviewDateTime!: Date | null;

  static schedule(recruitmentPeriodId: string, at: Date): InterviewTime {
    const interview = new InterviewTime();
    interview.recruitmentPeriodId = recruitmentPeriodId;
    interview.interviewDateTime = at;
    return interview;
  }
}
