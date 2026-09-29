import { Recruitment } from '../../domain/recruitment/recruitment';
import { NotFoundError } from '../../support/errors';
import { ApplicationService } from '../../support/stereotype';
import { apiId } from '../shared/api-id';
import { formatDate, seoulIso } from '../shared/dates';
import { RecruitmentFinder } from './provided/recruitment-finder';
import { RecruitmentResponse } from './provided/recruitment-responses';
import { RecruitmentRepository } from './required/recruitment-repository';

@ApplicationService()
export class RecruitmentQueryService implements RecruitmentFinder {
  constructor(private readonly recruitments: RecruitmentRepository) {}

  async getById(id: string): Promise<Recruitment> {
    const recruitment = await this.recruitments.findOneBy({ id });
    if (!recruitment)
      throw new NotFoundError('해당 모집기간이 존재하지 않습니다.');
    return recruitment;
  }
  private async getLatest(): Promise<Recruitment> {
    const recruitment = await this.recruitments.findOne({
      where: {},
      order: { semester: 'DESC', id: 'DESC' },
    });
    if (!recruitment)
      throw new NotFoundError('등록된 모집기간이 존재하지 않습니다.');
    return recruitment;
  }
  private response(recruitment: Recruitment): RecruitmentResponse {
    return {
      id: apiId(recruitment.id),
      semester: recruitment.semester,
      startDate: formatDate(recruitment.startDate, 'month'),
      endDate: formatDate(recruitment.endDate, 'month'),
      firstResultDate: formatDate(recruitment.firstResultDate, 'month'),
      finalResultDate: formatDate(recruitment.finalResultDate, 'month'),
      interviewStartDate: formatDate(recruitment.interviewStartDate, 'month'),
      interviewEndDate: formatDate(recruitment.interviewEndDate, 'month'),
      orientationDate: formatDate(recruitment.orientationDate, 'month'),
    };
  }
  async latest(): Promise<RecruitmentResponse> {
    return this.response(await this.getLatest());
  }
  async all(): Promise<RecruitmentResponse[]> {
    return (
      await this.recruitments.find({ order: { semester: 'DESC', id: 'DESC' } })
    ).map((recruitment) => this.response(recruitment));
  }
  async availability(today = seoulIso().slice(0, 10)): Promise<boolean> {
    return (await this.getLatest()).isOpenOn(today);
  }
}
