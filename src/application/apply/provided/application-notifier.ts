import { NotificationType } from '../../../domain/apply/application-result-log';
import { ResultDto } from './apply-requests';

export abstract class ApplicationNotifier {
  abstract confirmEmail(email: string): Promise<void>;
  /**
   * Sends the receipt (null) or result mail once per application and type.
   * Calling it again retries a failed mail. Concurrent sends are serialized,
   * but SMTP is not transactional, so a crash after delivery can duplicate one.
   */
  abstract notify(
    applicationId: string,
    type: NotificationType | null,
  ): Promise<void>;
  /** Mails every application with these decisions, across all semesters. */
  abstract notifyResults(request: ResultDto): Promise<void>;
}
