import { Body, Delete, Get, Param, Patch } from '@nestjs/common';
import { MailTemplateFinder } from '../../../application/mail/provided/mail-template-finder';
import { MailTemplateRegister } from '../../../application/mail/provided/mail-template-register';
import {
  MailTemplateDto,
  MailTemplateTypeParam,
} from '../../../application/mail/provided/mail-template-requests';
import { WebApiAdapter } from '../../../support/stereotype';
import { success } from '../api-response';
import { Access } from '../security/access';

@WebApiAdapter()
export class MailTemplateController {
  constructor(
    private readonly finder: MailTemplateFinder,
    private readonly register: MailTemplateRegister,
  ) {}
  @Get('admin/email/templates')
  @Access('admin')
  async list() {
    return success('메일 양식 조회 성공', await this.finder.list());
  }
  @Patch('admin/email/templates/:type')
  @Access('admin')
  async update(
    @Param() param: MailTemplateTypeParam,
    @Body() dto: MailTemplateDto,
  ) {
    await this.register.update(param.type, dto);
    return success('메일 양식 수정 성공');
  }
  @Delete('admin/email/templates/:type')
  @Access('admin')
  async reset(@Param() param: MailTemplateTypeParam) {
    await this.register.reset(param.type);
    return success('메일 양식 기본값 복원 성공');
  }
}
