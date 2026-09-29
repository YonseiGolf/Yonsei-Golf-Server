import {
  IsEnum,
  IsInt,
  IsNotEmpty,
  IsOptional,
  IsString,
  Length,
  Max,
  Min,
} from 'class-validator';
import { UserClass, UserProfile } from '../../../domain/user/user';
import { PageQuery } from '../../shared/page';

export class KakaoCodeDto {
  @IsString() @IsNotEmpty() kakaoCode!: string;
}
export class SignUpDto implements UserProfile {
  @IsString() @Length(1, 10) name!: string;
  @IsString() @Length(1, 15) phoneNumber!: string;
  @IsInt() @Min(1) @Max(99) studentId!: number;
  @IsString() @Length(1, 10) major!: string;
  @IsInt() @Min(1) @Max(99) semester!: number;
}
export class UserClassDto {
  @IsEnum(UserClass) userClass!: UserClass;
}
export class UsersQuery extends PageQuery {
  @IsOptional() @IsEnum(UserClass) userClass?: UserClass;
}
