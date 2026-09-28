import { Column, Entity } from 'typeorm';
import { BaseEntity } from '../database/base.entity';

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

  isAdmin(): boolean {
    return [
      UserRole.LEADER,
      UserRole.ASSISTANT_LEADER,
      UserRole.OB_LEADER,
      UserRole.OB_ASSISTANT_LEADER,
    ].includes(this.role);
  }
}
