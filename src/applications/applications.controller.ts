import {
  Body,
  Controller,
  Delete,
  Get,
  HttpCode,
  Param,
  Patch,
  Post,
  Query,
} from '@nestjs/common';
import { IdPipe, success } from '../common/http';
import { EmailService } from '../email/email.service';
import { ImageService } from '../storage/image.service';
import { Access } from '../users/auth';
import {
  ApplicationDto,
  AssignedInterviewDto,
  EmailAlarmDto,
  EmailDto,
  FormsQuery,
  ImageUploadDto,
  InterviewDto,
  PassDto,
  RecruitmentDto,
  ResultDto,
  SemesterQuery,
} from './applications.dto';
import { ApplicationsService } from './applications.service';
import { RecruitmentService } from './recruitment.service';

@Controller()
export class ApplicationsController {
  constructor(
    private readonly applications: ApplicationsService,
    private readonly recruitments: RecruitmentService,
    private readonly email: EmailService,
    private readonly images: ImageService,
  ) {}
  @Post('application')
  @HttpCode(200)
  async apply(@Body() dto: ApplicationDto) {
    await this.applications.apply(dto);
    return success('연세골프 지원서 제출 성공');
  }
  @Post('application/emailAlarm')
  @HttpCode(200)
  async emailAlarm(@Body() dto: EmailAlarmDto) {
    await this.applications.emailAlarm(dto);
    return success('연세골프 지원서 이메일 알림 설정 성공');
  }
  @Post('application/email-confirmation')
  @HttpCode(200)
  async confirmation(@Body() dto: EmailDto) {
    await this.email.confirmation(dto.email);
    return success('지원서 이메일 확인 메일 발송 성공');
  }
  @Get('application/recruit') async latest() {
    return success(
      '연세골프 지원 기간 조회 성공',
      await this.recruitments.latest(),
    );
  }
  @Get('application/recruit/:recruitId/interview-times') async interviews(
    @Param('recruitId', IdPipe) id: string,
  ) {
    return success(
      '면접 시간 목록 조회 성공',
      await this.recruitments.listInterviews(id),
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
    await this.recruitments.create(dto);
    return success('모집 기간 등록 성공');
  }
  @Patch('admin/recruit/:id')
  @Access('admin')
  async updateRecruitment(
    @Param('id', IdPipe) id: string,
    @Body() dto: RecruitmentDto,
  ) {
    await this.recruitments.update(id, dto);
    return success('모집 기간 수정 성공');
  }
  @Delete('admin/recruit/:id')
  @Access('admin')
  async removeRecruitment(@Param('id', IdPipe) id: string) {
    await this.recruitments.remove(id);
    return success('모집 기간 삭제 성공');
  }
  @Get('admin/recruit/:recruitId/interview-times')
  @Access('admin')
  async adminInterviews(@Param('recruitId', IdPipe) id: string) {
    return success(
      '면접 시간 목록 조회 성공',
      await this.recruitments.listInterviews(id),
    );
  }
  @Post('admin/recruit/:recruitId/interview-times')
  @HttpCode(200)
  @Access('admin')
  async createInterview(
    @Param('recruitId', IdPipe) id: string,
    @Body() dto: InterviewDto,
  ) {
    await this.recruitments.createInterview(id, dto);
    return success('면접 시간 등록 성공');
  }
  @Patch('admin/interview-times/:interviewTimeId')
  @Access('admin')
  async updateInterview(
    @Param('interviewTimeId', IdPipe) id: string,
    @Body() dto: InterviewDto,
  ) {
    await this.recruitments.updateInterview(id, dto);
    return success('면접 시간 수정 성공');
  }
  @Delete('admin/interview-times/:interviewTimeId')
  @Access('admin')
  async removeInterview(@Param('interviewTimeId', IdPipe) id: string) {
    await this.recruitments.removeInterview(id);
    return success('면접 시간 삭제 성공');
  }
  @Get('admin/forms')
  @Access('admin')
  async forms(@Query() query: FormsQuery) {
    return success(
      '연세골프 지원서 조회 성공',
      await this.applications.list(query),
    );
  }
  @Get('admin/forms/:id')
  @Access('admin')
  async detail(@Param('id', IdPipe) id: string) {
    return success(
      '연세골프 지원서 조회 성공',
      await this.applications.detail(id),
    );
  }
  @Patch('admin/forms/:id/pass')
  @Access('admin')
  async pass(@Param('id', IdPipe) id: string, @Body() dto: PassDto) {
    await this.applications.updatePass(id, dto);
    return success('연세골프 지원서 합격 여부 수정 성공');
  }
  @Patch('admin/forms/:id/interviewTime')
  @Access('admin')
  async assignInterview(
    @Param('id', IdPipe) id: string,
    @Body() dto: AssignedInterviewDto,
  ) {
    await this.applications.updateInterview(id, dto.time);
    return success('연세골프 지원서 면접 시간 수정 성공');
  }
  @Post('admin/forms/results')
  @HttpCode(200)
  @Access('admin')
  async results(@Body() dto: ResultDto) {
    await this.applications.sendResults(dto);
    return success('연세골프 지원서 결과 이메일 발송 성공');
  }
  @Post('apply/forms/image/presigned-url')
  @HttpCode(200)
  async presign(@Body() dto: ImageUploadDto) {
    return success(
      '연세골프 지원서 사진 업로드 URL 발급 성공',
      await this.images.presign(dto),
    );
  }
  @Get('admin/email/apply-start-email')
  @Access('admin')
  async waiting(@Query() query: SemesterQuery) {
    return success(
      '지원 시작 이메일 전송 대기 목록 조회 성공',
      await this.email.waiting(query.semester),
    );
  }
  @Post('admin/email/apply-start-email')
  @HttpCode(200)
  @Access('admin')
  async alerts() {
    await this.email.sendRecruitmentAlerts();
    return success('지원 시작 이메일 전송 성공');
  }
}
