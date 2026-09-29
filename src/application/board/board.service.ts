import { Board, Reply } from '../../domain/board/board';
import { NotFoundError } from '../../support/errors';
import { ApplicationService } from '../../support/stereotype';
import { BoardQueryService } from './board-query.service';
import { BoardRegister } from './provided/board-register';
import { BoardDto, ReplyDto } from './provided/board-requests';
import { BoardRepository } from './required/board-repository';
import { ReplyRepository } from './required/reply-repository';

@ApplicationService()
export class BoardService implements BoardRegister {
  constructor(
    private readonly boards: BoardRepository,
    private readonly replies: ReplyRepository,
    private readonly query: BoardQueryService,
  ) {}

  async create(request: BoardDto, userId: string): Promise<void> {
    await this.boards.save(Board.write(request, userId, new Date()));
  }
  async update(id: string, request: BoardDto, userId: string): Promise<void> {
    const board = await this.query.getVisible(id);
    board.edit(request, userId);
    await this.boards.update(id, {
      category: board.category,
      title: board.title,
      content: board.content,
    });
  }
  async remove(id: string, userId: string): Promise<void> {
    const board = await this.query.getVisible(id);
    board.delete(userId);
    await this.boards.update(id, { deleted: board.deleted });
  }
  async reply(
    boardId: string,
    request: ReplyDto,
    userId: string,
  ): Promise<void> {
    await this.query.getVisible(boardId);
    await this.replies.save(
      Reply.write(request.content, boardId, userId, new Date()),
    );
  }
  async removeReply(id: string, userId: string): Promise<void> {
    const reply = await this.replies.findOneBy({ id });
    if (!reply) throw new NotFoundError('해당 댓글이 존재하지 않습니다.');
    reply.checkDeletableBy(userId);
    await this.replies.delete(id);
  }
}
