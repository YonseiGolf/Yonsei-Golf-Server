import { RecruitmentResponse } from './recruitment-responses';

export abstract class RecruitmentFinder {
  /** The recruitment of the latest semester. */
  abstract latest(): Promise<RecruitmentResponse>;
  abstract all(): Promise<RecruitmentResponse[]>;
  /** Whether the latest recruitment accepts applications on `today` (KST `YYYY-MM-DD`). */
  abstract availability(today?: string): Promise<boolean>;
}
