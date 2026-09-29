import { JwtService } from '@nestjs/jwt';
import { apiId } from '../../../application/shared/api-id';
import {
  INVALID_TOKEN_MESSAGE,
  TokenIssuer,
  VerifiedToken,
} from '../../../application/user/required/token-issuer';
import { User } from '../../../domain/user/user';
import { UnauthenticatedError } from '../../../support/errors';
import { Adapter } from '../../../support/stereotype';
import { Settings } from '../../config/settings';

// HS256 with the Base64-decoded Spring secret and the `userProfile` claim,
// so tokens issued by the Spring server stay valid.
@Adapter()
export class JwtTokenIssuer implements TokenIssuer {
  constructor(
    private readonly jwt: JwtService,
    private readonly settings: Settings,
  ) {}
  issueOAuthToken(kakaoId: string): string {
    return this.jwt.sign(
      { kind: 'oauth', userProfile: { id: apiId(kakaoId) } },
      {
        secret: this.settings.jwtSecret,
        algorithm: 'HS256',
        subject: 'login_member',
        expiresIn: 360,
      },
    );
  }
  issueAccessToken(user: User): string {
    return this.jwt.sign(
      {
        kind: 'user',
        userProfile: {
          id: apiId(user.id),
          name: user.name,
          adminStatus: user.isAdmin(),
          memberStatus: user.hasMemberStatus(),
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
  verify(token: string): VerifiedToken {
    try {
      const payload = this.jwt.verify<{
        sub: string;
        kind?: string;
        userProfile: { id: string | number; name?: string };
      }>(token, {
        secret: this.settings.jwtSecret,
        algorithms: ['HS256'],
        subject: 'login_member',
      });
      const profile = payload.userProfile;
      const id = profile?.id;
      if (
        (typeof id !== 'string' && typeof id !== 'number') ||
        !/^[1-9]\d*$/.test(String(id)) ||
        (typeof id === 'number' && !Number.isSafeInteger(id))
      )
        throw new Error('Invalid principal');
      // Spring tokens have no `kind`; only the member token carries a name.
      const kind =
        payload.kind ?? (profile.name === undefined ? 'oauth' : 'user');
      if (kind !== 'oauth' && kind !== 'user')
        throw new Error('Wrong token kind');
      return { principalId: String(id), kind };
    } catch (error) {
      // The cause (e.g. "jwt expired") is logged, not returned.
      throw new UnauthenticatedError(INVALID_TOKEN_MESSAGE, { cause: error });
    }
  }
}
