import { Type } from 'class-transformer';
import { IsDateString, IsInt, Matches, Max, Min } from 'class-validator';
import { RecruitmentSchedule } from '../../../domain/recruitment/recruitment';
import { EmailDto } from '../../shared/email';

export class RecruitmentDto implements RecruitmentSchedule {
  @IsInt() @Min(1) @Max(9999) semester!: number;
  @IsDateString({ strict: true })
  @Matches(/^\d{4}-\d{2}-\d{2}$/)
  startDate!: string;
  @IsDateString({ strict: true })
  @Matches(/^\d{4}-\d{2}-\d{2}$/)
  endDate!: string;
  @IsDateString({ strict: true })
  @Matches(/^\d{4}-\d{2}-\d{2}$/)
  firstResultDate!: string;
  @IsDateString({ strict: true })
  @Matches(/^\d{4}-\d{2}-\d{2}$/)
  finalResultDate!: string;
  @IsDateString({ strict: true })
  @Matches(/^\d{4}-\d{2}-\d{2}$/)
  interviewStartDate!: string;
  @IsDateString({ strict: true })
  @Matches(/^\d{4}-\d{2}-\d{2}$/)
  interviewEndDate!: string;
  @IsDateString({ strict: true })
  @Matches(/^\d{4}-\d{2}-\d{2}$/)
  orientationDate!: string;
}
export class InterviewDto {
  @IsDateString({ strict: true }) interviewDateTime!: string;
}
// Semester 0 means "not assigned yet": visitors subscribe before a recruitment is announced.
export class EmailAlarmDto extends EmailDto {
  @Type(() => Number) @IsInt() @Min(0) @Max(9999) semester!: number;
}
export class SemesterQuery {
  @Type(() => Number) @IsInt() @Min(0) @Max(9999) semester!: number;
}
