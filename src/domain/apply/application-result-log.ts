import { Column, Entity } from 'typeorm';
import { BaseEntity } from '../common/base-entity';

export enum NotificationType {
  DOCUMENT_PASS = 'DOCUMENT_PASS',
  FINAL_PASS = 'FINAL_PASS',
  FAIL = 'FAIL',
}

/** The result an admin announces for applicants with these decisions. */
export function notificationTypeOf(
  documentPass: boolean,
  finalPass: boolean | null,
): NotificationType {
  if (documentPass && finalPass === null) return NotificationType.DOCUMENT_PASS;
  if (documentPass && finalPass === true) return NotificationType.FINAL_PASS;
  return NotificationType.FAIL;
}

/** One row per mail sent to an application; a null type is the receipt mail. */
@Entity('application_result_log')
export class ApplicationResultLog extends BaseEntity {
  @Column({ name: 'application_id', type: 'bigint', nullable: true })
  applicationId!: string;
  @Column({
    name: 'notification_type',
    type: 'varchar',
    length: 64,
    nullable: true,
  })
  notificationType!: NotificationType | null;
  @Column({ name: 'sent_at', type: 'datetime', precision: 6, nullable: true })
  sentAt!: Date | null;

  static record(
    applicationId: string,
    type: NotificationType | null,
    at: Date,
  ): ApplicationResultLog {
    const log = new ApplicationResultLog();
    log.applicationId = applicationId;
    log.notificationType = type;
    log.sentAt = at;
    return log;
  }
}
