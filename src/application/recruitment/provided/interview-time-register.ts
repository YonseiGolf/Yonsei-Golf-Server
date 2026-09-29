import { InterviewDto } from './recruitment-requests';

/** Interview times are Korean wall-clock times. */
export abstract class InterviewTimeRegister {
  abstract create(recruitmentId: string, request: InterviewDto): Promise<void>;
  abstract update(id: string, request: InterviewDto): Promise<void>;
  abstract remove(id: string): Promise<void>;
}
