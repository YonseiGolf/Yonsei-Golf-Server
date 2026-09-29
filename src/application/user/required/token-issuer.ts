import { User } from '../../../domain/user/user';

export const INVALID_TOKEN_MESSAGE = '유효하지 않거나 만료된 토큰입니다.';

export interface VerifiedToken {
  principalId: string;
  kind: 'oauth' | 'user';
}

export abstract class TokenIssuer {
  /** A short-lived token that only allows signing up or signing in. */
  abstract issueOAuthToken(kakaoId: string): string;
  abstract issueAccessToken(user: User): string;
  /** Throws UnauthenticatedError(INVALID_TOKEN_MESSAGE) with the reason as its cause. */
  abstract verify(token: string): VerifiedToken;
}
