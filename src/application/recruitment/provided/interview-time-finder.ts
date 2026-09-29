import { InterviewTimeResponse } from './recruitment-responses';

/** Interview times are listed in time order. */
export abstract class InterviewTimeFinder {
  abstract listByRecruitment(
    recruitmentId: string,
  ): Promise<InterviewTimeResponse[]>;
  abstract findByIds(ids: readonly string[]): Promise<InterviewTimeResponse[]>;
  /** Whether every ID is an interview time of a recruitment for `semester`. */
  abstract allBelongToSemester(
    ids: readonly string[],
    semester: number,
  ): Promise<boolean>;
}
