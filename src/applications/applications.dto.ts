import { Transform, Type } from 'class-transformer';
import {
  ArrayMaxSize,
  ArrayUnique,
  IsArray,
  IsBoolean,
  IsDateString,
  IsEmail,
  IsInt,
  IsNotEmpty,
  IsOptional,
  IsString,
  Matches,
  Max,
  MaxLength,
  Min,
  ValidateIf,
  ValidateNested,
} from 'class-validator';
import { PageQuery } from '../common/http';

export class RecruitmentDto {
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
export class ActivityDto {
  @IsString() @IsNotEmpty() @MaxLength(60) clubName!: string;
  @IsOptional()
  @IsDateString({ strict: true })
  @Matches(/^\d{4}-\d{2}-\d{2}$/)
  startDate: string | null = null;
  @IsOptional()
  @IsDateString({ strict: true })
  @Matches(/^\d{4}-\d{2}-\d{2}$/)
  endDate: string | null = null;
  @IsOptional() @IsString() @MaxLength(1000) role: string | null = null;
}
export class ApplicationDto {
  @IsString() @IsNotEmpty() @MaxLength(255) name!: string;
  @IsOptional()
  @IsString()
  @Matches(/^store-image\/[A-Za-z0-9_-]+\.(jpg|png|webp|gif)$/)
  @MaxLength(1024)
  photoKey: string | null = null;
  @IsOptional()
  @IsDateString({ strict: true })
  @Matches(/^\d{4}-\d{2}-\d{2}$/)
  birthDate: string | null = null;
  @IsInt() @Min(1) @Max(Number.MAX_SAFE_INTEGER) studentId!: number;
  @IsEmail() @MaxLength(255) email!: string;
  @IsString() @IsNotEmpty() @MaxLength(25) major!: string;
  @IsString() @IsNotEmpty() @MaxLength(25) phoneNumber!: string;
  @IsString() @IsNotEmpty() @MaxLength(16000) selfIntroduction!: string;
  @IsString() @IsNotEmpty() @MaxLength(16000) applyReason!: string;
  @IsString() @IsNotEmpty() @MaxLength(16000) skillEvaluation!: string;
  @IsString() @IsNotEmpty() @MaxLength(16000) golfMemory!: string;
  @IsString() @MaxLength(3000) swingVideo!: string;
  @IsOptional() @IsDateString({ strict: true }) submitTime?: string;
  @IsInt() @Min(1) @Max(9999) semester!: number;
  @IsArray()
  @ArrayMaxSize(30)
  @ValidateNested({ each: true })
  @Type(() => ActivityDto)
  activityClubs: ActivityDto[] = [];
  @IsArray()
  @ArrayUnique()
  @ArrayMaxSize(100)
  @IsInt({ each: true })
  @Min(1, { each: true })
  @Max(Number.MAX_SAFE_INTEGER, { each: true })
  availableInterviewTimeIds: number[] = [];
}
export class EmailDto {
  @IsEmail() @MaxLength(255) email!: string;
}
// Semester 0 means "not assigned yet": visitors subscribe before a recruitment is announced.
export class EmailAlarmDto extends EmailDto {
  @Type(() => Number) @IsInt() @Min(0) @Max(9999) semester!: number;
}
export class SemesterQuery {
  @Type(() => Number) @IsInt() @Min(0) @Max(9999) semester!: number;
}
export class PassDto {
  @ValidateIf((_object, value) => value !== null) @IsBoolean() documentPass!:
    | boolean
    | null;
  @ValidateIf((_object, value) => value !== null) @IsBoolean() finalPass!:
    | boolean
    | null;
}
export class ResultDto {
  @IsBoolean() documentPass!: boolean;
  @IsOptional() @IsBoolean() finalPass: boolean | null = null;
}
export class AssignedInterviewDto {
  @IsOptional() @IsDateString({ strict: true }) time: string | null = null;
}
const booleanQuery = ({ value }: { value: unknown }) =>
  value === 'true'
    ? true
    : value === 'false'
      ? false
      : value === 'null'
        ? null
        : value;
export class FormsQuery extends PageQuery {
  @Type(() => Number) @IsInt() @Min(1) @Max(9999) semester!: number;
  @Transform(booleanQuery) @IsOptional() @IsBoolean() documentPass:
    | boolean
    | null = null;
  @Transform(booleanQuery) @IsOptional() @IsBoolean() finalPass:
    | boolean
    | null = null;
}
export class ImageUploadDto {
  @IsString() @IsNotEmpty() @MaxLength(255) fileName!: string;
  @IsString() @Matches(/^image\/(jpeg|png|webp|gif)$/) contentType!: string;
  @IsInt() @Min(1) @Max(10 * 1024 * 1024) fileSize!: number;
}
