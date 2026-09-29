import { Type } from 'class-transformer';
import { IsInt, Max, Min } from 'class-validator';

export class PageQuery {
  @Type(() => Number) @IsInt() @Min(0) @Max(1000000) page = 0;
  @Type(() => Number) @IsInt() @Min(1) @Max(100) size = 20;
}

interface Sort {
  empty: boolean;
  sorted: boolean;
  unsorted: boolean;
}

/** The Spring Data `Page` JSON the clients were built against. */
export interface PageResponse<T> {
  content: T[];
  pageable: {
    sort: Sort;
    offset: number;
    pageNumber: number;
    pageSize: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  size: number;
  number: number;
  sort: Sort;
  numberOfElements: number;
  first: boolean;
  empty: boolean;
}

export function pageResponse<T>(
  content: T[],
  total: number,
  query: PageQuery,
): PageResponse<T> {
  const sort = { empty: true, sorted: false, unsorted: true };
  const totalPages = Math.ceil(total / query.size);
  return {
    content,
    pageable: {
      sort,
      offset: query.page * query.size,
      pageNumber: query.page,
      pageSize: query.size,
      paged: true,
      unpaged: false,
    },
    totalElements: total,
    totalPages,
    last: query.page >= totalPages - 1,
    size: query.size,
    number: query.page,
    sort,
    numberOfElements: content.length,
    first: query.page === 0,
    empty: content.length === 0,
  };
}
