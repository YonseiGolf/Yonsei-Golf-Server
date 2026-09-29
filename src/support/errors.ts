// Expected failures thrown from any layer. The message is returned to the
// client as is and the cause only reaches the request log. The web adapter
// (ApiExceptionFilter) picks the HTTP status, so inner layers never see HTTP.
export abstract class ExpectedError extends Error {
  constructor(message: string, options?: { cause?: unknown }) {
    super(message, options);
    this.name = new.target.name;
  }
}

export class InvalidInputError extends ExpectedError {}
export class UnauthenticatedError extends ExpectedError {}
export class ForbiddenError extends ExpectedError {}
export class NotFoundError extends ExpectedError {}
export class ConflictError extends ExpectedError {}
export class ExternalServiceError extends ExpectedError {}
