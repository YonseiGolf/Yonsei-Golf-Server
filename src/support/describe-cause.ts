import { HttpException } from '@nestjs/common';
import { ExpectedError } from './errors';

/** A short reason for logs; unwraps public errors to the error they were created from. */
export function describeCause(cause: unknown): string | undefined {
  if (cause === undefined || cause === null) return undefined;
  if (
    (cause instanceof HttpException || cause instanceof ExpectedError) &&
    cause.cause !== undefined
  )
    return describeCause(cause.cause);
  if (cause instanceof Error) {
    const code = (cause as { code?: unknown }).code;
    const suffix = typeof code === 'string' ? ` (${code})` : '';
    return `${cause.name}: ${cause.message}${suffix}`.slice(0, 500);
  }
  const text = typeof cause === 'string' ? cause : JSON.stringify(cause);
  return text.slice(0, 500);
}
