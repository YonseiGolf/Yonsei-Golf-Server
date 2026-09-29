import {
  ArgumentsHost,
  Catch,
  ExceptionFilter,
  HttpException,
} from '@nestjs/common';
import { Response } from 'express';
import { QueryFailedError } from 'typeorm';
import { describeCause } from '../../support/describe-cause';
import {
  ConflictError,
  ExpectedError,
  ExternalServiceError,
  ForbiddenError,
  InvalidInputError,
  NotFoundError,
  UnauthenticatedError,
} from '../../support/errors';
import { RequestFailure } from './request-logger';

// Inner layers throw ExpectedError subtypes; only this adapter knows HTTP.
function expectedStatus(error: unknown): number | undefined {
  if (error instanceof InvalidInputError) return 400;
  if (error instanceof UnauthenticatedError) return 401;
  if (error instanceof ForbiddenError) return 403;
  if (error instanceof NotFoundError) return 404;
  if (error instanceof ConflictError) return 409;
  if (error instanceof ExternalServiceError) return 502;
  return undefined;
}

// The failure reason goes to the request log (requestLogger) so each request
// is one log line. Clients only get the public message.
@Catch()
export class ApiExceptionFilter implements ExceptionFilter {
  catch(error: unknown, host: ArgumentsHost): void {
    let status = 500;
    let message = '서버 오류가 발생했습니다.';
    let failure: RequestFailure;
    const expected = expectedStatus(error);
    if (error instanceof ExpectedError && expected !== undefined) {
      status = expected;
      message = error.message;
      failure = { error: message, cause: describeCause(error.cause) };
    } else if (error instanceof HttpException) {
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
