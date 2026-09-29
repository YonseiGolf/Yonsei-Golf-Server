import { Column, Entity } from 'typeorm';
import { BaseEntity } from '../common/base-entity';

/** A visitor's request to be told by email when a recruitment starts. */
@Entity('email_alarm')
export class EmailAlarm extends BaseEntity {
  @Column({ type: 'varchar', length: 255 }) email!: string;
  @Column({ type: 'bigint', nullable: true }) semester!: string | null;
  @Column({ name: 'sent_at', type: 'datetime', precision: 6, nullable: true })
  sentAt!: Date | null;

  /** Semester 0 means "not assigned yet": visitors subscribe before a recruitment is announced. */
  static subscribe(email: string, semester: number): EmailAlarm {
    const alarm = new EmailAlarm();
    alarm.email = email;
    alarm.semester = String(semester);
    alarm.sentAt = null;
    return alarm;
  }

  isSent(): boolean {
    return !!this.sentAt;
  }

  markSent(at: Date): void {
    this.sentAt = at;
  }
}
