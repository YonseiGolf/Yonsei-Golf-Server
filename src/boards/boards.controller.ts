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
import { Access, PrincipalId } from '../users/auth';
import { BoardDto, BoardsQuery, ReplyDto, TemplateDto } from './boards.dto';
import { BoardsService } from './boards.service';

@Controller()
export class BoardsController {
  constructor(private readonly boards: BoardsService) {}
  @Get('boards') async list(@Query() query: BoardsQuery) {
    return success('전체 게시글 조회 성공', await this.boards.list(query));
  }
  @Get('boards/:boardId') async detail(@Param('boardId', IdPipe) id: string) {
    return success('게시글 조회 성공', await this.boards.detail(id));
  }
  @Post('boards')
  @HttpCode(200)
  @Access('user')
  async create(@Body() dto: BoardDto, @PrincipalId() userId: string) {
    await this.boards.create(dto, userId);
    return success('게시글 생성 성공');
  }
  @Patch('boards/:boardId')
  @Access('user')
  async update(
    @Param('boardId', IdPipe) id: string,
    @Body() dto: BoardDto,
    @PrincipalId() userId: string,
  ) {
    await this.boards.update(id, dto, userId);
    return success('게시글 수정 성공');
  }
  @Delete('boards/:boardId')
  @Access('user')
  async remove(
    @Param('boardId', IdPipe) id: string,
    @PrincipalId() userId: string,
  ) {
    await this.boards.remove(id, userId);
    return success('게시글 삭제 성공');
  }
  @Post('boards/:boardId/replies')
  @HttpCode(200)
  @Access('user')
  async reply(
    @Param('boardId', IdPipe) id: string,
    @Body() dto: ReplyDto,
    @PrincipalId() userId: string,
  ) {
    await this.boards.reply(id, dto, userId);
    return success('댓글 생성 성공');
  }
  @Delete('replies/:replyId')
  @Access('user')
  async removeReply(
    @Param('replyId', IdPipe) id: string,
    @PrincipalId() userId: string,
  ) {
    await this.boards.removeReply(id, userId);
    return success('댓글 삭제 성공');
  }

  @Get('admin/boards/templates') @Access('admin') async templates() {
    return success(
      '게시글 템플릿 조회 성공',
      await this.boards.listTemplates(),
    );
  }
  @Get('admin/boards/templates/:templateId') @Access('admin') async template(
    @Param('templateId', IdPipe) id: string,
  ) {
    return success('게시글 템플릿 조회 성공', await this.boards.template(id));
  }
  @Post('admin/boards/templates')
  @HttpCode(200)
  @Access('admin')
  async createTemplate(@Body() dto: TemplateDto) {
    await this.boards.createTemplate(dto);
    return success('게시글 템플릿 생성 성공');
  }
  @Patch('admin/boards/templates/:templateId')
  @Access('admin')
  async updateTemplate(
    @Param('templateId', IdPipe) id: string,
    @Body() dto: TemplateDto,
  ) {
    await this.boards.updateTemplate(id, dto);
    return success('게시글 템플릿 수정 성공');
  }
  @Delete('admin/boards/templates/:templateId')
  @Access('admin')
  async removeTemplate(@Param('templateId', IdPipe) id: string) {
    await this.boards.removeTemplate(id);
    return success('게시글 템플릿 삭제 성공');
  }
}
