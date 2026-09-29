import { Category } from '../../../domain/board/board';

export interface BoardSummaryResponse {
  id: number | string;
  category: Category | null;
  title: string;
  writer: string | undefined;
  createdAt: string | null;
}

export interface ReplyResponse {
  id: number | string;
  writerId: number | string;
  writer: string;
  content: string;
  createdAt: string | null;
}

export interface BoardDetailResponse {
  id: number | string;
  writerId: number | string;
  writer: string | undefined;
  category: Category | null;
  title: string;
  content: string;
  createdAt: string | null;
  replies: { replies: ReplyResponse[] };
}

export interface TemplateSummaryResponse {
  id: number | string;
  title: string;
}

export interface TemplateResponse extends TemplateSummaryResponse {
  contents: string;
}
