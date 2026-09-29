import {
  applyDecorators,
  CanActivate,
  createParamDecorator,
  ExecutionContext,
  Injectable,
  SetMetadata,
  UnauthorizedException,
  UseGuards,
} from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import {
  AccessLevel,
  AccessVerifier,
} from '../../../application/user/provided/access-verifier';
import { LoggedRequest } from '../request-logger';

type AuthRequest = LoggedRequest & { principalId: string };

@Injectable()
export class AccessGuard implements CanActivate {
  constructor(
    private readonly reflector: Reflector,
    private readonly access: AccessVerifier,
  ) {}
  async canActivate(context: ExecutionContext): Promise<boolean> {
    const level =
      this.reflector.getAllAndOverride<AccessLevel>('access', [
        context.getHandler(),
        context.getClass(),
      ]) ?? 'user';
    const request = context.switchToHttp().getRequest<AuthRequest>();
    const match = request.headers.authorization?.match(/^Bearer (\S+)$/);
    if (!match?.[1])
      throw new UnauthorizedException('Bearer 인증이 필요합니다.');
    // Set before the DB check so a rejected member still appears in the request log.
    request.principalId = this.access.authenticate(match[1], level);
    request.principalKind = level === 'oauth' ? 'oauth' : 'user';
    await this.access.authorize(request.principalId, level);
    return true;
  }
}

export const Access = (level: AccessLevel) =>
  applyDecorators(SetMetadata('access', level), UseGuards(AccessGuard));
export const PrincipalId = createParamDecorator(
  (_data: unknown, context: ExecutionContext): string =>
    context.switchToHttp().getRequest<AuthRequest>().principalId,
);
