import { ForbiddenError, UnauthenticatedError } from '../../support/errors';
import { ApplicationService } from '../../support/stereotype';
import { AccessLevel, AccessVerifier } from './provided/access-verifier';
import {
  KakaoLogin,
  KakaoSession,
  LoginManager,
} from './provided/login-manager';
import { KakaoOAuthClient } from './required/kakao-oauth-client';
import { INVALID_TOKEN_MESSAGE, TokenIssuer } from './required/token-issuer';
import { UserQueryService } from './user-query.service';

@ApplicationService()
export class AuthService implements LoginManager, AccessVerifier {
  constructor(
    private readonly kakao: KakaoOAuthClient,
    private readonly tokens: TokenIssuer,
    private readonly users: UserQueryService,
  ) {}

  async loginWithKakao(code: string): Promise<KakaoLogin> {
    const account = await this.kakao.exchange(code);
    return {
      accessToken: this.tokens.issueOAuthToken(account.id),
      kakaoRefreshToken: account.refreshToken,
    };
  }
  async refreshKakaoSession(refreshToken: string): Promise<KakaoSession> {
    const account = await this.kakao.refresh(refreshToken);
    return { kakaoId: account.id, kakaoRefreshToken: account.refreshToken };
  }
  async signIn(kakaoId: string): Promise<string> {
    return this.tokens.issueAccessToken(await this.users.getByKakaoId(kakaoId));
  }

  authenticate(token: string, level: AccessLevel): string {
    const verified = this.tokens.verify(token);
    if ((level === 'oauth') !== (verified.kind === 'oauth'))
      throw new UnauthenticatedError(INVALID_TOKEN_MESSAGE, {
        cause: new Error('Wrong token kind'),
      });
    return verified.principalId;
  }
  async authorize(principalId: string, level: AccessLevel): Promise<void> {
    if (level === 'oauth') return;
    const user = await this.users.getById(principalId);
    if (user.isBlocked()) throw new ForbiddenError('접근이 제한된 회원입니다.');
    if (level === 'admin' && !user.isAdmin())
      throw new ForbiddenError('관리자 권한이 필요합니다.');
  }
}
