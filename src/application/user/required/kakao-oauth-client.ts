export interface KakaoAccount {
  id: string;
  refreshToken?: string;
}

/**
 * Kakao's token and user APIs. A rejected code or token throws
 * UnauthenticatedError; an unreachable or malformed API throws ExternalServiceError.
 */
export abstract class KakaoOAuthClient {
  abstract exchange(code: string): Promise<KakaoAccount>;
  abstract refresh(refreshToken: string): Promise<KakaoAccount>;
}
