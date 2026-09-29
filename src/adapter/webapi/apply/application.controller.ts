import { Body, Get, HttpCode, Param, Patch, Post, Query } from '@nestjs/common';
import { ApplicationFinder } from '../../../application/apply/provided/application-finder';
import { ApplicationNotifier } from '../../../application/apply/provided/application-notifier';
import { ApplicationPhotoUploader } from '../../../application/apply/provided/application-photo-uploader';
import { ApplicationRegister } from '../../../application/apply/provided/application-register';
import {
  ApplicationDto,
  AssignedInterviewDto,
  FormsQuery,
  ImageUploadDto,
  PassDto,
  ResultDto,
} from '../../../application/apply/provided/apply-requests';
import { EmailDto } from '../../../application/shared/email';
import { WebApiAdapter } from '../../../support/stereotype';
import { success } from '../api-response';
import { IdPipe } from '../id.pipe';
import { Access } from '../security/access';

@WebApiAdapter()
export class ApplicationController {
  constructor(
    private readonly register: ApplicationRegister,
    private readonly finder: ApplicationFinder,
    private readonly notifier: ApplicationNotifier,
    private readonly photos: ApplicationPhotoUploader,
  ) {}
  @Post('application')
  @HttpCode(200)
  async apply(@Body() dto: ApplicationDto) {
    await this.register.apply(dto);
    return success('연세골프 지원서 제출 성공');
  }
  @Post('application/email-confirmation')
  @HttpCode(200)
  async confirmation(@Body() dto: EmailDto) {
    await this.notifier.confirmEmail(dto.email);
    return success('지원서 이메일 확인 메일 발송 성공');
  }
  @Get('admin/forms')
  @Access('admin')
  async forms(@Query() query: FormsQuery) {
    return success('연세골프 지원서 조회 성공', await this.finder.list(query));
  }
  @Get('admin/forms/:id')
  @Access('admin')
  async detail(@Param('id', IdPipe) id: string) {
    return success('연세골프 지원서 조회 성공', await this.finder.detail(id));
  }
  @Patch('admin/forms/:id/pass')
  @Access('admin')
  async pass(@Param('id', IdPipe) id: string, @Body() dto: PassDto) {
    await this.register.updatePass(id, dto);
    return success('연세골프 지원서 합격 여부 수정 성공');
  }
  @Patch('admin/forms/:id/interviewTime')
  @Access('admin')
  async assignInterview(
    @Param('id', IdPipe) id: string,
    @Body() dto: AssignedInterviewDto,
  ) {
    await this.register.updateInterview(id, dto.time);
    return success('연세골프 지원서 면접 시간 수정 성공');
  }
  @Post('admin/forms/results')
  @HttpCode(200)
  @Access('admin')
  async results(@Body() dto: ResultDto) {
    await this.notifier.notifyResults(dto);
    return success('연세골프 지원서 결과 이메일 발송 성공');
  }
  @Post('apply/forms/image/presigned-url')
  @HttpCode(200)
  async presign(@Body() dto: ImageUploadDto) {
    return success(
      '연세골프 지원서 사진 업로드 URL 발급 성공',
      await this.photos.issueUploadUrl(dto),
    );
  }
}
