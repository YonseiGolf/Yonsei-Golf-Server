import { randomUUID } from 'node:crypto';
import {
  ConsoleLogger,
  HttpException,
  Logger,
  LoggerService,
} from '@nestjs/common';
import { NextFunction, Request, Response } from 'express';

export type LogFormat = 'json' | 'text';

// Production logs are one JSON object per line so Grafana can filter by field,
// e.g. {container="yg-server"} | json | status >= 500
export function createLogger(format: LogFormat): LoggerService {
  return format === 'json'
    ? new ConsoleLogger({ json: true, flattenParams: true })
    : new ConsoleLogger();
}

/** What the exception filter tells the request log about a failed request. */
export interface RequestFailure {
  error: string;
  cause?: string;
  stack?: string;
}

export type LoggedRequest = Request & {
  principalId?: string;
  principalKind?: 'oauth' | 'user';
};

const REQUEST_ID = /^[A-Za-z0-9._:-]{1,100}$/;
const SECRET_QUERY_KEY = /token|code|secret|password|key/i;

/** A short reason for logs; unwraps Nest exceptions to the error they were created from. */
export function describeCause(cause: unknown): string | undefined {
  if (cause === undefined || cause === null) return undefined;
  if (cause instanceof HttpException && cause.cause !== undefined)
    return describeCause(cause.cause);
  if (cause instanceof Error) {
    const code = (cause as { code?: unknown }).code;
    const suffix = typeof code === 'string' ? ` (${code})` : '';
    return `${cause.name}: ${cause.message}${suffix}`.slice(0, 500);
  }
  const text = typeof cause === 'string' ? cause : JSON.stringify(cause);
  return text.slice(0, 500);
}

function queryString(url: string): string | undefined {
  const start = url.indexOf('?');
  if (start < 0) return undefined;
  const params = new URLSearchParams(url.slice(start + 1));
  for (const key of [...params.keys()])
    if (SECRET_QUERY_KEY.test(key)) params.set(key, '[redacted]');
  return params.toString() || undefined;
}

/**
 * Logs one line per request when the response ends: method, path, status,
 * duration, request ID, caller and, for failures, the reason from the
 * exception filter. Request bodies, headers and cookies are never logged.
 */
export function requestLogger() {
  const logger = new Logger('HTTP');
  return (
    request: LoggedRequest,
    response: Response,
    next: NextFunction,
  ): void => {
    const started = process.hrtime.bigint();
    const requestId =
      [request.header('x-request-id'), request.header('cf-ray')].find(
        (value) => value !== undefined && REQUEST_ID.test(value),
      ) ?? randomUUID();
    response.setHeader('X-Request-Id', requestId);
    response.on('close', () => {
      const path = request.originalUrl.split('?')[0];
      const status = response.statusCode;
      // Docker and the external uptime checks call this several times a minute.
      if (path === '/healthcheck' && status < 400) return;
      const aborted = !response.writableFinished;
      const durationMs =
        Math.round(Number(process.hrtime.bigint() - started) / 1e5) / 10;
      const failure = response.locals.failure as RequestFailure | undefined;
      const principal =
        request.principalKind === 'oauth'
          ? { kakaoId: request.principalId }
          : { userId: request.principalId };
      const fields = Object.fromEntries(
        Object.entries({
          requestId,
          method: request.method,
          path,
          route: (request.route as { path?: string } | undefined)?.path,
          query: queryString(request.originalUrl),
          status,
          durationMs,
          aborted: aborted || undefined,
          ...principal,
          ip: request.header('cf-connecting-ip') ?? request.ip,
          userAgent: request.header('user-agent')?.slice(0, 200),
          error: failure?.error,
          cause: failure?.cause,
        }).filter(([, value]) => value !== undefined),
      );
      const message = `${request.method} ${path} ${status} ${durationMs}ms${aborted ? ' (aborted)' : ''}`;
      if (status >= 500)
        logger.error(
          message,
          fields,
          ...(failure?.stack ? [failure.stack] : []),
        );
      else if (status >= 400 || aborted) logger.warn(message, fields);
      else logger.log(message, fields);
    });
    next();
  };
}
