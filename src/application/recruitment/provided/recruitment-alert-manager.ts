import { EmailAlarmDto } from './recruitment-requests';
import { EmailAlarmResponse } from './recruitment-responses';

/** Emails visitors who asked to be told when a recruitment starts. */
export abstract class RecruitmentAlertManager {
  abstract subscribe(request: EmailAlarmDto): Promise<void>;
  abstract waiting(
    semester: number,
  ): Promise<{ emailAlarms: EmailAlarmResponse[] }>;
  /**
   * Sends each unsent alert once. A failed delivery stays unsent so calling
   * again retries it; SMTP is not transactional, so a crash can still duplicate one.
   */
  abstract sendAll(): Promise<void>;
}
