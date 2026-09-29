import { Recruitment } from '../../domain/recruitment/recruitment';
import { ApplicationService } from '../../support/stereotype';
import { RecruitmentRegister } from './provided/recruitment-register';
import { RecruitmentDto } from './provided/recruitment-requests';
import { RecruitmentQueryService } from './recruitment-query.service';
import { RecruitmentRepository } from './required/recruitment-repository';

@ApplicationService()
export class RecruitmentService implements RecruitmentRegister {
  constructor(
    private readonly recruitments: RecruitmentRepository,
    private readonly query: RecruitmentQueryService,
  ) {}

  async create(request: RecruitmentDto): Promise<void> {
    await this.recruitments.save(Recruitment.open(request));
  }
  async update(id: string, request: RecruitmentDto): Promise<void> {
    const recruitment = await this.query.getById(id);
    recruitment.reschedule(request);
    await this.recruitments.update(id, {
      semester: recruitment.semester,
      startDate: recruitment.startDate,
      endDate: recruitment.endDate,
      firstResultDate: recruitment.firstResultDate,
      interviewStartDate: recruitment.interviewStartDate,
      interviewEndDate: recruitment.interviewEndDate,
      finalResultDate: recruitment.finalResultDate,
      orientationDate: recruitment.orientationDate,
    });
  }
  async remove(id: string): Promise<void> {
    await this.query.getById(id);
    await this.recruitments.delete(id);
  }
}
