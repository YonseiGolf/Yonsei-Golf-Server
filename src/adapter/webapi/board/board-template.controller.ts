import {
  Body,
  Delete,
  Get,
  HttpCode,
  Param,
  Patch,
  Post,
} from '@nestjs/common';
import { TemplateDto } from '../../../application/board/provided/board-requests';
import { BoardTemplateFinder } from '../../../application/board/provided/board-template-finder';
import { BoardTemplateRegister } from '../../../application/board/provided/board-template-register';
import { WebApiAdapter } from '../../../support/stereotype';
import { success } from '../api-response';
import { IdPipe } from '../id.pipe';
import { Access } from '../security/access';

@WebApiAdapter()
export class BoardTemplateController {
  constructor(
    private readonly finder: BoardTemplateFinder,
    private readonly register: BoardTemplateRegister,
  ) {}
  @Get('admin/boards/templates') @Access('admin') async templates() {
    return success('게시글 템플릿 조회 성공', await this.finder.list());
  }
  @Get('admin/boards/templates/:templateId') @Access('admin') async template(
    @Param('templateId', IdPipe) id: string,
  ) {
    return success('게시글 템플릿 조회 성공', await this.finder.find(id));
  }
  @Post('admin/boards/templates')
  @HttpCode(200)
  @Access('admin')
  async createTemplate(@Body() dto: TemplateDto) {
    await this.register.create(dto);
    return success('게시글 템플릿 생성 성공');
  }
  @Patch('admin/boards/templates/:templateId')
  @Access('admin')
  async updateTemplate(
    @Param('templateId', IdPipe) id: string,
    @Body() dto: TemplateDto,
  ) {
    await this.register.update(id, dto);
    return success('게시글 템플릿 수정 성공');
  }
  @Delete('admin/boards/templates/:templateId')
  @Access('admin')
  async removeTemplate(@Param('templateId', IdPipe) id: string) {
    await this.register.remove(id);
    return success('게시글 템플릿 삭제 성공');
  }
}
