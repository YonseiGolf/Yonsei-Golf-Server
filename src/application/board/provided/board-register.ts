import { BoardDto, ReplyDto } from './board-requests';

/** Only the writer may edit or delete a board or reply. */
export abstract class BoardRegister {
  abstract create(request: BoardDto, userId: string): Promise<void>;
  abstract update(id: string, request: BoardDto, userId: string): Promise<void>;
  /** Soft delete: the row stays with `deleted` set. */
  abstract remove(id: string, userId: string): Promise<void>;
  abstract reply(
    boardId: string,
    request: ReplyDto,
    userId: string,
  ): Promise<void>;
  abstract removeReply(id: string, userId: string): Promise<void>;
}
