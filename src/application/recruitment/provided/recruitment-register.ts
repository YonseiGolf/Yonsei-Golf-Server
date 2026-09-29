import { RecruitmentDto } from './recruitment-requests';

export abstract class RecruitmentRegister {
  abstract create(request: RecruitmentDto): Promise<void>;
  abstract update(id: string, request: RecruitmentDto): Promise<void>;
  /** A recruitment with interview times is kept by the foreign key (409). */
  abstract remove(id: string): Promise<void>;
}
