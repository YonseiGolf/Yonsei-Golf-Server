import {
  Body,
  Delete,
  Get,
  HttpCode,
  Param,
  Patch,
  Post,
  Query,
} from '@nestjs/common';
import { BoardFinder } from '../../../application/board/provided/board-finder';
import { BoardRegister } from '../../../application/board/provided/board-register';
import {
  BoardDto,
  BoardsQuery,
  ReplyDto,
} from '../../../application/board/provided/board-requests';
import { WebApiAdapter } from '../../../support/stereotype';
import { success } from '../api-response';
import { IdPipe } from '../id.pipe';
import { Access, PrincipalId } from '../security/access';

@WebApiAdapter()
export class BoardController {
  constructor(
    private readonly finder: BoardFinder,
    private readonly register: BoardRegister,
  ) {}
  @Get('boards') async list(@Query() query: BoardsQuery) {
    return success('전체 게시글 조회 성공', await this.finder.list(query));
  }
  @Get('boards/:boardId') async detail(@Param('boardId', IdPipe) id: string) {
    return success('게시글 조회 성공', await this.finder.detail(id));
  }
  @Post('boards')
  @HttpCode(200)
  @Access('user')
  async create(@Body() dto: BoardDto, @PrincipalId() userId: string) {
    await this.register.create(dto, userId);
    return success('게시글 생성 성공');
  }
  @Patch('boards/:boardId')
  @Access('user')
  async update(
    @Param('boardId', IdPipe) id: string,
    @Body() dto: BoardDto,
    @PrincipalId() userId: string,
  ) {
    await this.register.update(id, dto, userId);
    return success('게시글 수정 성공');
  }
  @Delete('boards/:boardId')
  @Access('user')
  async remove(
    @Param('boardId', IdPipe) id: string,
    @PrincipalId() userId: string,
  ) {
    await this.register.remove(id, userId);
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
    await this.register.reply(id, dto, userId);
    return success('댓글 생성 성공');
  }
  @Delete('replies/:replyId')
  @Access('user')
  async removeReply(
    @Param('replyId', IdPipe) id: string,
    @PrincipalId() userId: string,
  ) {
    await this.register.removeReply(id, userId);
    return success('댓글 삭제 성공');
  }
}
