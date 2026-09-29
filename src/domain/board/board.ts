import { Column, Entity, JoinColumn, ManyToOne } from 'typeorm';
import { ForbiddenError } from '../../support/errors';
import { BaseEntity, bitBoolean } from '../common/base-entity';
import { User } from '../user/user';

export enum Category {
  NOTICE = 'NOTICE',
  FREE = 'FREE',
}

export interface BoardPost {
  category: Category;
  title: string;
  content: string;
}

function checkWriter(writerId: string, userId: string): void {
  if (String(writerId) !== userId)
    throw new ForbiddenError('작성자만 수정/삭제할 수 있습니다.');
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

  static write(post: BoardPost, userId: string, now: Date): Board {
    const board = new Board();
    board.category = post.category;
    board.title = post.title;
    board.content = post.content;
    board.userId = userId;
    board.createdAt = now;
    board.deleted = false;
    return board;
  }

  /** Soft-deleted boards and boards whose writer row is gone are not shown. */
  isVisible(): boolean {
    return !this.deleted && !!this.writer;
  }

  edit(post: BoardPost, userId: string): void {
    checkWriter(this.userId, userId);
    this.category = post.category;
    this.title = post.title;
    this.content = post.content;
  }

  delete(userId: string): void {
    checkWriter(this.userId, userId);
    this.deleted = true;
  }
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

  static write(
    content: string,
    boardId: string,
    userId: string,
    now: Date,
  ): Reply {
    const reply = new Reply();
    reply.content = content;
    reply.boardId = boardId;
    reply.userId = userId;
    reply.createdAt = now;
    return reply;
  }

  checkDeletableBy(userId: string): void {
    checkWriter(this.userId, userId);
  }
}
