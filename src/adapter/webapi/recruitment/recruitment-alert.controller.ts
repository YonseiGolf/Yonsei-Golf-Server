import { Body, Get, HttpCode, Post, Query } from '@nestjs/common';
import { RecruitmentAlertManager } from '../../../application/recruitment/provided/recruitment-alert-manager';
import {
  EmailAlarmDto,
  SemesterQuery,
} from '../../../application/recruitment/provided/recruitment-requests';
import { WebApiAdapter } from '../../../support/stereotype';
import { success } from '../api-response';
import { Access } from '../security/access';

@WebApiAdapter()
export class RecruitmentAlertController {
  constructor(private readonly alerts: RecruitmentAlertManager) {}
  @Post('application/emailAlarm')
  @HttpCode(200)
  async subscribe(@Body() dto: EmailAlarmDto) {
    await this.alerts.subscribe(dto);
    return success('연세골프 지원서 이메일 알림 설정 성공');
  }
  @Get('admin/email/apply-start-email')
  @Access('admin')
  async waiting(@Query() query: SemesterQuery) {
    return success(
      '지원 시작 이메일 전송 대기 목록 조회 성공',
      await this.alerts.waiting(query.semester),
    );
  }
  @Post('admin/email/apply-start-email')
  @HttpCode(200)
  @Access('admin')
  async send() {
    await this.alerts.sendAll();
    return success('지원 시작 이메일 전송 성공');
  }
}
