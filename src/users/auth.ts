import {
  applyDecorators,
  CanActivate,
  createParamDecorator,
  ExecutionContext,
  ForbiddenException,
  Injectable,
  SetMetadata,
  UnauthorizedException,
  UseGuards,
} from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { JwtService } from '@nestjs/jwt';
import { Request } from 'express';
import { apiId } from '../common/http';
import { Settings } from '../config/settings';
import { User, UserClass } from './user.entity';
import { UsersService } from './users.service';

type AccessLevel = 'oauth' | 'user' | 'admin';
type AuthRequest = Request & {
  principalId: string;
  principalKind?: 'oauth' | 'user';
};

@Injectable()
export class AuthService {
  constructor(
    private readonly jwt: JwtService,
    private readonly settings: Settings,
  ) {}
  oauthToken(id: string): string {
    return this.jwt.sign(
      { kind: 'oauth', userProfile: { id: apiId(id) } },
      {
        secret: this.settings.jwtSecret,
        algorithm: 'HS256',
        subject: 'login_member',
        expiresIn: 360,
      },
    );
  }
  accessToken(user: User): string {
    return this.jwt.sign(
      {
        kind: 'user',
        userProfile: {
          id: apiId(user.id),
          name: user.name,
          adminStatus: user.isAdmin(),
          memberStatus: user.userClass !== UserClass.NONE,
        },
      },
      {
        secret: this.settings.jwtSecret,
        algorithm: 'HS256',
        subject: 'login_member',
        expiresIn: 3600,
      },
    );
  }
  verify(header: string | undefined, level: AccessLevel): string {
    const match = header?.match(/^Bearer (\S+)$/);
    if (!match?.[1])
      throw new UnauthorizedException('Bearer 인증이 필요합니다.');
    try {
      const token = this.jwt.verify<{
        sub: string;
        kind?: string;
        userProfile: { id: string | number; name?: string };
      }>(match[1], {
        secret: this.settings.jwtSecret,
        algorithms: ['HS256'],
        subject: 'login_member',
      });
      const profile = token.userProfile;
      const id = profile?.id;
      if (
        (typeof id !== 'string' && typeof id !== 'number') ||
        !/^[1-9]\d*$/.test(String(id)) ||
        (typeof id === 'number' && !Number.isSafeInteger(id))
      )
        throw new Error('Invalid principal');
      const kind =
        token.kind ?? (profile.name === undefined ? 'oauth' : 'user');
      if (
        (level === 'oauth' && kind !== 'oauth') ||
        (level !== 'oauth' && kind !== 'user')
      )
        throw new Error('Wrong token kind');
      return String(id);
    } catch (error) {
      // The cause (e.g. "jwt expired", "Wrong token kind") is logged, not returned.
      throw new UnauthorizedException('유효하지 않거나 만료된 토큰입니다.', {
        cause: error,
      });
    }
  }
}

@Injectable()
export class AuthGuard implements CanActivate {
  constructor(
    private readonly reflector: Reflector,
    private readonly auth: AuthService,
    private readonly users: UsersService,
  ) {}
  async canActivate(context: ExecutionContext): Promise<boolean> {
    const level =
      this.reflector.getAllAndOverride<AccessLevel>('access', [
        context.getHandler(),
        context.getClass(),
      ]) ?? 'user';
    const request = context.switchToHttp().getRequest<AuthRequest>();
    request.principalId = this.auth.verify(
      request.headers.authorization,
      level,
    );
    request.principalKind = level === 'oauth' ? 'oauth' : 'user';
    if (level !== 'oauth') {
      const user = await this.users.findById(request.principalId);
      if (user.userClass === UserClass.BLACK_LIST)
        throw new ForbiddenException('접근이 제한된 회원입니다.');
      if (level === 'admin' && !user.isAdmin())
        throw new ForbiddenException('관리자 권한이 필요합니다.');
    }
    return true;
  }
}

export const Access = (level: AccessLevel) =>
  applyDecorators(SetMetadata('access', level), UseGuards(AuthGuard));
export const PrincipalId = createParamDecorator(
  (_data: unknown, context: ExecutionContext): string =>
    context.switchToHttp().getRequest<AuthRequest>().principalId,
);
