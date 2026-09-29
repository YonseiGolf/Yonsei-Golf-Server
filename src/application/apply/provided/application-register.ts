import { ApplicationDto, PassDto } from './apply-requests';

export abstract class ApplicationRegister {
  /**
   * Stores the application, its club activities and interview choices in one
   * transaction, then sends the receipt mail. A mail failure is logged and
   * does not undo the submitted application.
   */
  abstract apply(request: ApplicationDto): Promise<void>;
  abstract updatePass(id: string, request: PassDto): Promise<void>;
  /** `time` is a Korean wall-clock time; null clears the assignment. */
  abstract updateInterview(id: string, time: string | null): Promise<void>;
}
