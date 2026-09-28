import { Column, Entity, JoinColumn, ManyToOne } from 'typeorm';
import { BaseEntity, bitBoolean } from '../database/base.entity';
import { User } from '../users/user.entity';

export enum Category {
  NOTICE = 'NOTICE',
  FREE = 'FREE',
}

@Entity('board')
export class Board extends BaseEntity {
  @Column({ type: 'varchar', length: 255 }) title!: string;
  @Column({ type: 'text' }) content!: string;
  @Column({ type: 'varchar', length: 255, nullable: true })
  category!: Category | null;
  @Column({ name: 'created_at', type: 'datetime', nullable: true })
  createdAt!: Date | null;
  @Column({ type: 'bit', nullable: true, transformer: bitBoolean }) deleted!:
    | boolean
    | null;
  @Column({ name: 'user_id', type: 'bigint', nullable: true }) userId!: string;
  @ManyToOne(() => User, { nullable: true, createForeignKeyConstraints: false })
  @JoinColumn({ name: 'user_id' })
  writer!: User | null;
}

@Entity('reply')
export class Reply extends BaseEntity {
  @Column({ type: 'text' }) content!: string;
  @Column({ name: 'created_at', type: 'datetime' }) createdAt!: Date;
  @Column({ name: 'board_id', type: 'bigint' }) boardId!: string;
  @Column({ name: 'user_id', type: 'bigint' }) userId!: string;
  @ManyToOne(() => User, { createForeignKeyConstraints: false })
  @JoinColumn({ name: 'user_id' })
  writer!: User;
}

@Entity('board_template')
export class BoardTemplate extends BaseEntity {
  @Column({ type: 'varchar', length: 255 }) title!: string;
  @Column({ type: 'text' }) contents!: string;
}
