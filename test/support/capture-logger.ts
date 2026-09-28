import { LoggerService } from '@nestjs/common';

export interface LogEntry {
  level: string;
  message: unknown;
  context?: string;
  fields: Record<string, unknown>;
  stack?: string;
}

const isPlainObject = (value: unknown): value is Record<string, unknown> =>
  typeof value === 'object' &&
  value !== null &&
  Object.getPrototypeOf(value) === Object.prototype;

/** Keeps log entries in memory so tests can assert on them (and keeps test output quiet). */
export class CaptureLogger implements LoggerService {
  readonly entries: LogEntry[] = [];
  private add(level: string, message: unknown, params: unknown[]) {
    const context =
      typeof params.at(-1) === 'string' ? (params.pop() as string) : undefined;
    this.entries.push({
      level,
      message,
      context,
      fields: Object.assign({}, ...params.filter(isPlainObject)),
      stack: params.find(
        (param): param is string =>
          typeof param === 'string' && /\n\s+at /.test(param),
      ),
    });
  }
  log(message: unknown, ...params: unknown[]) {
    this.add('log', message, params);
  }
  warn(message: unknown, ...params: unknown[]) {
    this.add('warn', message, params);
  }
  error(message: unknown, ...params: unknown[]) {
    this.add('error', message, params);
  }
  debug(message: unknown, ...params: unknown[]) {
    this.add('debug', message, params);
  }
  verbose(message: unknown, ...params: unknown[]) {
    this.add('verbose', message, params);
  }
  fatal(message: unknown, ...params: unknown[]) {
    this.add('fatal', message, params);
  }
}
