import { Board } from '../../domain/board/board';
import { NotFoundError } from '../../support/errors';
import { ApplicationService } from '../../support/stereotype';
import { apiId } from '../shared/api-id';
import { formatDate } from '../shared/dates';
import { PageResponse, pageResponse } from '../shared/page';
import { BoardFinder } from './provided/board-finder';
import { BoardsQuery } from './provided/board-requests';
import {
  BoardDetailResponse,
  BoardSummaryResponse,
} from './provided/board-responses';
import { BoardRepository } from './required/board-repository';
import { ReplyRepository } from './required/reply-repository';

@ApplicationService()
export class BoardQueryService implements BoardFinder {
  constructor(
    private readonly boards: BoardRepository,
    private readonly replies: ReplyRepository,
  ) {}

  async getVisible(id: string): Promise<Board> {
    const board = await this.boards.findOne({
      where: { id },
      relations: { writer: true },
    });
    if (!board?.isVisible())
      throw new NotFoundError('해당 게시글이 존재하지 않습니다.');
    return board;
  }
  async list(query: BoardsQuery): Promise<PageResponse<BoardSummaryResponse>> {
    const builder = this.boards
      .createQueryBuilder('board')
      .innerJoinAndSelect('board.writer', 'writer')
      .where('board.deleted = :deleted', { deleted: 0 })
      .orderBy('board.id', 'DESC')
      .skip(query.page * query.size)
      .take(query.size);
    if (query.category)
      builder.andWhere('board.category = :category', {
        category: query.category,
      });
    const [boards, total] = await builder.getManyAndCount();
    return pageResponse(
      boards.map((board) => ({
        id: apiId(board.id),
        category: board.category,
        title: board.title,
        writer: board.writer?.name,
        createdAt: formatDate(board.createdAt, 'short'),
      })),
      total,
      query,
    );
  }
  async detail(id: string): Promise<BoardDetailResponse> {
    const board = await this.getVisible(id);
    const replies = await this.replies.find({
      where: { boardId: id },
      relations: { writer: true },
      order: { id: 'ASC' },
    });
    return {
      id: apiId(board.id),
      writerId: apiId(board.userId),
      writer: board.writer?.name,
      category: board.category,
      title: board.title,
      content: board.content,
      createdAt: formatDate(board.createdAt, 'full'),
      replies: {
        replies: replies.map((reply) => ({
          id: apiId(reply.id),
          writerId: apiId(reply.userId),
          writer: reply.writer?.name ?? '',
          content: reply.content,
          createdAt: formatDate(reply.createdAt, 'full'),
        })),
      },
    };
  }
}
