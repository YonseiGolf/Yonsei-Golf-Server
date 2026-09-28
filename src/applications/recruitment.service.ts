import {
  BadRequestException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { formatDate, localDateTime, seoulIso } from '../common/dates';
import { apiId } from '../common/http';
import { InterviewTime, Recruitment } from './application.entity';
import { InterviewDto, RecruitmentDto } from './applications.dto';

export const interviewResponse = (interview: InterviewTime) => ({
  id: apiId(interview.id),
  interviewDateTime: formatDate(interview.interviewDateTime, 'interview'),
});

@Injectable()
export class RecruitmentService {
  constructor(
    @InjectRepository(Recruitment)
    private readonly recruitments: Repository<Recruitment>,
    @InjectRepository(InterviewTime)
    private readonly interviews: Repository<InterviewTime>,
  ) {}
  async find(id: string) {
    const recruitment = await this.recruitments.findOneBy({ id });
    if (!recruitment)
      throw new NotFoundException('해당 모집기간이 존재하지 않습니다.');
    return recruitment;
  }
  private response(recruitment: Recruitment) {
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
  private async latestEntity() {
    const recruitment = await this.recruitments.findOne({
      where: {},
      order: { semester: 'DESC', id: 'DESC' },
    });
    if (!recruitment)
      throw new NotFoundException('등록된 모집기간이 존재하지 않습니다.');
    return recruitment;
  }
  async latest() {
    return this.response(await this.latestEntity());
  }
  async all() {
    return (
      await this.recruitments.find({ order: { semester: 'DESC', id: 'DESC' } })
    ).map((recruitment) => this.response(recruitment));
  }
  async availability(today = seoulIso().slice(0, 10)) {
    const recruitment = await this.latestEntity();
    return today >= recruitment.startDate && today <= recruitment.endDate;
  }
  private validateDates(dto: RecruitmentDto): void {
    if (
      dto.startDate > dto.endDate ||
      dto.interviewStartDate > dto.interviewEndDate
    )
      throw new BadRequestException('시작일은 종료일보다 늦을 수 없습니다.');
  }
  async create(dto: RecruitmentDto): Promise<void> {
    this.validateDates(dto);
    await this.recruitments.save(this.recruitments.create(dto));
  }
  async update(id: string, dto: RecruitmentDto): Promise<void> {
    await this.find(id);
    this.validateDates(dto);
    await this.recruitments.update(id, dto);
  }
  async remove(id: string): Promise<void> {
    await this.find(id);
    await this.recruitments.delete(id);
  }
  async listInterviews(recruitmentPeriodId: string) {
    await this.find(recruitmentPeriodId);
    return (
      await this.interviews.find({
        where: { recruitmentPeriodId },
        order: { interviewDateTime: 'ASC', id: 'ASC' },
      })
    ).map(interviewResponse);
  }
  async createInterview(
    recruitmentPeriodId: string,
    dto: InterviewDto,
  ): Promise<void> {
    await this.find(recruitmentPeriodId);
    await this.interviews.save(
      this.interviews.create({
        recruitmentPeriodId,
        interviewDateTime: localDateTime(dto.interviewDateTime),
      }),
    );
  }
  private async interview(id: string) {
    const interview = await this.interviews.findOneBy({ id });
    if (!interview)
      throw new NotFoundException('해당 면접 시간이 존재하지 않습니다.');
    return interview;
  }
  async updateInterview(id: string, dto: InterviewDto): Promise<void> {
    await this.interview(id);
    await this.interviews.update(id, {
      interviewDateTime: localDateTime(dto.interviewDateTime),
    });
  }
  async removeInterview(id: string): Promise<void> {
    await this.interview(id);
    await this.interviews.delete(id);
  }
}
