import { PageResponse } from '../../shared/page';
import { BoardsQuery } from './board-requests';
import { BoardDetailResponse, BoardSummaryResponse } from './board-responses';

export abstract class BoardFinder {
  abstract list(
    query: BoardsQuery,
  ): Promise<PageResponse<BoardSummaryResponse>>;
  abstract detail(id: string): Promise<BoardDetailResponse>;
}
