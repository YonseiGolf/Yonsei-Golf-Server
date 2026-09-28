import {
  ArgumentsHost,
  BadRequestException,
  Catch,
  ExceptionFilter,
  HttpException,
  PipeTransform,
} from '@nestjs/common';
import { Type } from 'class-transformer';
import { IsInt, Max, Min } from 'class-validator';
import { Response } from 'express';
import { QueryFailedError } from 'typeorm';
import { describeCause, RequestFailure } from './logging';

export const success = <T>(message: string, data: T | null = null) => ({
  status: 'success',
  code: 200,
  message,
  data,
});

// Keep numeric API IDs when safe; never silently round a MySQL BIGINT.
export function apiId(value: string | number): number | string {
  const number = Number(value);
  return Number.isSafeInteger(number) ? number : String(value);
}

export class IdPipe implements PipeTransform<string, string> {
  transform(value: string): string {
    if (!/^[1-9]\d*$/.test(value) || BigInt(value) > 9223372036854775807n)
      throw new BadRequestException('올바른 ID를 입력해주세요.');
    return value;
  }
}

export class PageQuery {
  @Type(() => Number) @IsInt() @Min(0) @Max(1000000) page = 0;
  @Type(() => Number) @IsInt() @Min(1) @Max(100) size = 20;
}

export function pageResponse<T>(content: T[], total: number, query: PageQuery) {
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

// The failure reason goes to the request log (requestLogger) so each request
// is one log line. Clients only get the public message.
@Catch()
export class ApiExceptionFilter implements ExceptionFilter {
  catch(error: unknown, host: ArgumentsHost): void {
    let status = 500;
    let message = '서버 오류가 발생했습니다.';
    let failure: RequestFailure;
    if (error instanceof HttpException) {
      status = error.getStatus();
      const response = error.getResponse();
      message =
        typeof response === 'string'
          ? response
          : (response as { message?: string | string[] }).message?.toString() ||
            error.message;
      failure = { error: message, cause: describeCause(error.cause) };
    } else if (error instanceof QueryFailedError) {
      const driver = error.driverError as {
        code?: string;
        sqlMessage?: string;
      };
      if (
        driver.code === 'ER_ROW_IS_REFERENCED_2' ||
        driver.code === 'ER_NO_REFERENCED_ROW_2' ||
        driver.code === 'ER_DUP_ENTRY'
      ) {
        status = 409;
        message = '연결된 데이터가 있거나 이미 존재하는 데이터입니다.';
        // The key or constraint name only: a duplicate entry message contains the stored value.
        const key = driver.sqlMessage?.match(
          /(?:for key|CONSTRAINT) [`']([^`']+)/,
        )?.[1];
        failure = {
          error: message,
          cause: `${driver.code}${key ? ` ${key}` : ''}`,
        };
      } else {
        failure = {
          error: 'Database error',
          cause:
            `${driver.code ?? 'unknown'}: ${driver.sqlMessage ?? error.message}`.slice(
              0,
              500,
            ),
          stack: error.stack,
        };
      }
    } else {
      failure = {
        error: error instanceof Error ? error.message : 'Unknown error',
        cause: describeCause(error instanceof Error ? error.cause : error),
        stack: error instanceof Error ? error.stack : undefined,
      };
    }
    const response = host.switchToHttp().getResponse<Response>();
    response.locals.failure = failure;
    response
      .status(status)
      .json({ status: 'error', code: status, message, data: null });
  }
}
