import { In } from 'typeorm';
import { InterviewTime } from '../../domain/recruitment/recruitment';
import { NotFoundError } from '../../support/errors';
import { ApplicationService } from '../../support/stereotype';
import { apiId } from '../shared/api-id';
import { formatDate, localDateTime } from '../shared/dates';
import { InterviewTimeFinder } from './provided/interview-time-finder';
import { InterviewTimeRegister } from './provided/interview-time-register';
import { InterviewDto } from './provided/recruitment-requests';
import { InterviewTimeResponse } from './provided/recruitment-responses';
import { RecruitmentQueryService } from './recruitment-query.service';
import { InterviewTimeRepository } from './required/interview-time-repository';
import { RecruitmentRepository } from './required/recruitment-repository';

const response = (interview: InterviewTime): InterviewTimeResponse => ({
  id: apiId(interview.id),
  interviewDateTime: formatDate(interview.interviewDateTime, 'interview'),
});

@ApplicationService()
export class InterviewTimeService
  implements InterviewTimeRegister, InterviewTimeFinder
{
  constructor(
    private readonly interviews: InterviewTimeRepository,
    private readonly recruitments: RecruitmentRepository,
    private readonly query: RecruitmentQueryService,
  ) {}

  async listByRecruitment(
    recruitmentId: string,
  ): Promise<InterviewTimeResponse[]> {
    await this.query.getById(recruitmentId);
    return (
      await this.interviews.find({
        where: { recruitmentPeriodId: recruitmentId },
        order: { interviewDateTime: 'ASC', id: 'ASC' },
      })
    ).map(response);
  }
  async findByIds(ids: readonly string[]): Promise<InterviewTimeResponse[]> {
    if (!ids.length) return [];
    return (
      await this.interviews.find({
        where: { id: In([...ids]) },
        order: { interviewDateTime: 'ASC', id: 'ASC' },
      })
    ).map(response);
  }
  async allBelongToSemester(
    ids: readonly string[],
    semester: number,
  ): Promise<boolean> {
    const times = await this.interviews.find({ where: { id: In([...ids]) } });
    const periods = await this.recruitments.find({
      where: { id: In(times.map((time) => time.recruitmentPeriodId)) },
    });
    return (
      times.length === ids.length &&
      periods.length > 0 &&
      periods.every((period) => period.semester === semester)
    );
  }
  async create(recruitmentId: string, request: InterviewDto): Promise<void> {
    await this.query.getById(recruitmentId);
    await this.interviews.save(
      InterviewTime.schedule(
        recruitmentId,
        localDateTime(request.interviewDateTime),
      ),
    );
  }
  private async getById(id: string): Promise<InterviewTime> {
    const interview = await this.interviews.findOneBy({ id });
    if (!interview)
      throw new NotFoundError('해당 면접 시간이 존재하지 않습니다.');
    return interview;
  }
  async update(id: string, request: InterviewDto): Promise<void> {
    await this.getById(id);
    await this.interviews.update(id, {
      interviewDateTime: localDateTime(request.interviewDateTime),
    });
  }
  async remove(id: string): Promise<void> {
    await this.getById(id);
    await this.interviews.delete(id);
  }
}
