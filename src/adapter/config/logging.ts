import { ConsoleLogger, LoggerService } from '@nestjs/common';

export type LogFormat = 'json' | 'text';

// Production logs are one JSON object per line so Grafana can filter by field,
// e.g. {container="yg-server"} | json | status >= 500
export function createLogger(format: LogFormat): LoggerService {
  return format === 'json'
    ? new ConsoleLogger({ json: true, flattenParams: true })
    : new ConsoleLogger();
}
