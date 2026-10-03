import { Column, Entity } from 'typeorm';
import { BaseEntity } from '../common/base-entity';

/** A result stage. Each one is mailed to an application at most once. */
export enum NotificationType {
  DOCUMENT_PASS = 'DOCUMENT_PASS',
  DOCUMENT_FAIL = 'DOCUMENT_FAIL',
  FINAL_PASS = 'FINAL_PASS',
  FINAL_FAIL = 'FINAL_FAIL',
}

/** The result an admin announces for applicants with these decisions. */
export function notificationTypeOf(
  documentPass: boolean,
  finalPass: boolean | null,
): NotificationType {
  if (!documentPass) return NotificationType.DOCUMENT_FAIL;
  if (finalPass === null) return NotificationType.DOCUMENT_PASS;
  return finalPass ? NotificationType.FINAL_PASS : NotificationType.FINAL_FAIL;
}

/** The mail an application's current decisions call for; null is the receipt of an undecided one. */
export function dueNotificationOf(
  documentPass: boolean | null,
  finalPass: boolean | null,
): NotificationType | null {
  return documentPass === null
    ? null
    : notificationTypeOf(documentPass, finalPass);
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
