import {
  Body,
  Delete,
  Get,
  HttpCode,
  Param,
  Patch,
  Post,
} from '@nestjs/common';
import { InterviewTimeFinder } from '../../../application/recruitment/provided/interview-time-finder';
import { InterviewTimeRegister } from '../../../application/recruitment/provided/interview-time-register';
import { RecruitmentFinder } from '../../../application/recruitment/provided/recruitment-finder';
import { RecruitmentRegister } from '../../../application/recruitment/provided/recruitment-register';
import {
  InterviewDto,
  RecruitmentDto,
} from '../../../application/recruitment/provided/recruitment-requests';
import { WebApiAdapter } from '../../../support/stereotype';
import { success } from '../api-response';
import { IdPipe } from '../id.pipe';
import { Access } from '../security/access';

@WebApiAdapter()
export class RecruitmentController {
  constructor(
    private readonly recruitments: RecruitmentFinder,
    private readonly recruitmentRegister: RecruitmentRegister,
    private readonly interviews: InterviewTimeFinder,
    private readonly interviewRegister: InterviewTimeRegister,
  ) {}
  @Get('application/recruit') async latest() {
    return success(
      '연세골프 지원 기간 조회 성공',
      await this.recruitments.latest(),
    );
  }
  @Get('application/recruit/:recruitId/interview-times') async interviewTimes(
    @Param('recruitId', IdPipe) id: string,
  ) {
    return success(
      '면접 시간 목록 조회 성공',
      await this.interviews.listByRecruitment(id),
    );
  }
  @Get('application/availability') async availability() {
    return success(
      '연세골프 지원 가능 여부 조회 성공',
      await this.recruitments.availability(),
    );
  }
  @Get('admin/recruit') @Access('admin') async adminLatest() {
    return success('모집 기간 조회 성공', await this.recruitments.latest());
  }
  @Get('admin/recruits') @Access('admin') async all() {
    return success('모집 기간 목록 조회 성공', await this.recruitments.all());
  }
  @Post('admin/recruit')
  @HttpCode(200)
  @Access('admin')
  async createRecruitment(@Body() dto: RecruitmentDto) {
    await this.recruitmentRegister.create(dto);
    return success('모집 기간 등록 성공');
  }
  @Patch('admin/recruit/:id')
  @Access('admin')
  async updateRecruitment(
    @Param('id', IdPipe) id: string,
    @Body() dto: RecruitmentDto,
  ) {
    await this.recruitmentRegister.update(id, dto);
    return success('모집 기간 수정 성공');
  }
  @Delete('admin/recruit/:id')
  @Access('admin')
  async removeRecruitment(@Param('id', IdPipe) id: string) {
    await this.recruitmentRegister.remove(id);
    return success('모집 기간 삭제 성공');
  }
  @Get('admin/recruit/:recruitId/interview-times')
  @Access('admin')
  async adminInterviewTimes(@Param('recruitId', IdPipe) id: string) {
    return success(
      '면접 시간 목록 조회 성공',
      await this.interviews.listByRecruitment(id),
    );
  }
  @Post('admin/recruit/:recruitId/interview-times')
  @HttpCode(200)
  @Access('admin')
  async createInterviewTime(
    @Param('recruitId', IdPipe) id: string,
    @Body() dto: InterviewDto,
  ) {
    await this.interviewRegister.create(id, dto);
    return success('면접 시간 등록 성공');
  }
  @Patch('admin/interview-times/:interviewTimeId')
  @Access('admin')
  async updateInterviewTime(
    @Param('interviewTimeId', IdPipe) id: string,
    @Body() dto: InterviewDto,
  ) {
    await this.interviewRegister.update(id, dto);
    return success('면접 시간 수정 성공');
  }
  @Delete('admin/interview-times/:interviewTimeId')
  @Access('admin')
  async removeInterviewTime(@Param('interviewTimeId', IdPipe) id: string) {
    await this.interviewRegister.remove(id);
    return success('면접 시간 삭제 성공');
  }
}
