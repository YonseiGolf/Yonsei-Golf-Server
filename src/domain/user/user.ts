import { Column, Entity } from 'typeorm';
import { ConflictError } from '../../support/errors';
import { BaseEntity } from '../common/base-entity';

export enum UserRole {
  LEADER = 'LEADER',
  ASSISTANT_LEADER = 'ASSISTANT_LEADER',
  MEMBER = 'MEMBER',
  OB_LEADER = 'OB_LEADER',
  OB_ASSISTANT_LEADER = 'OB_ASSISTANT_LEADER',
}
export enum UserClass {
  YB = 'YB',
  OB = 'OB',
  NONE = 'NONE',
  BLACK_LIST = 'BLACK_LIST',
  DORMANT = 'DORMANT',
}

export interface UserProfile {
  name: string;
  phoneNumber: string;
  studentId: number;
  major: string;
  semester: number;
}

// Members imported from the Spring era have kakao_id 0 until they sign up.
const UNLINKED_KAKAO_ID = '0';

@Entity('user')
export class User extends BaseEntity {
  @Column({ name: 'kakao_id', type: 'bigint' }) kakaoId!: string;
  @Column({ type: 'varchar', length: 255 }) name!: string;
  @Column({ name: 'phone_number', type: 'varchar', length: 255 })
  phoneNumber!: string;
  @Column({ name: 'student_id', type: 'int' }) studentId!: number;
  @Column({ type: 'varchar', length: 255 }) major!: string;
  @Column({ type: 'int' }) semester!: number;
  @Column({ type: 'varchar', length: 255 }) role!: UserRole;
  @Column({ name: 'user_class', type: 'varchar', length: 255 })
  userClass!: UserClass;

  static register(profile: UserProfile, kakaoId: string): User {
    const user = new User();
    user.applyProfile(profile);
    user.kakaoId = kakaoId;
    user.role = UserRole.MEMBER;
    user.userClass = UserClass.NONE;
    return user;
  }

  /** Links an existing member to a Kakao account, keeping role and class. */
  linkKakao(profile: UserProfile, kakaoId: string): void {
    if (this.kakaoId !== UNLINKED_KAKAO_ID && this.kakaoId !== kakaoId)
      throw new ConflictError('이미 다른 카카오 계정에 연결된 회원입니다.');
    this.applyProfile(profile);
    this.kakaoId = kakaoId;
  }

  changeClass(userClass: UserClass): void {
    this.userClass = userClass;
  }

  isAdmin(): boolean {
    return [
      UserRole.LEADER,
      UserRole.ASSISTANT_LEADER,
      UserRole.OB_LEADER,
      UserRole.OB_ASSISTANT_LEADER,
    ].includes(this.role);
  }

  isBlocked(): boolean {
    return this.userClass === UserClass.BLACK_LIST;
  }

  /** The legacy `memberStatus` token claim: any class has been assigned. */
  hasMemberStatus(): boolean {
    return this.userClass !== UserClass.NONE;
  }

  private applyProfile(profile: UserProfile): void {
    this.name = profile.name;
    this.phoneNumber = profile.phoneNumber;
    this.studentId = profile.studentId;
    this.major = profile.major;
    this.semester = profile.semester;
  }
}
