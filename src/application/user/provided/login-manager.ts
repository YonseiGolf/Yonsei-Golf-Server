export interface KakaoLogin {
  /** The pre-sign-up (`oauth`) token for the Kakao account. */
  accessToken: string;
  kakaoRefreshToken?: string;
}

export interface KakaoSession {
  kakaoId: string;
  /** Present when Kakao rotated the refresh token. */
  kakaoRefreshToken?: string;
}

export abstract class LoginManager {
  abstract loginWithKakao(code: string): Promise<KakaoLogin>;
  abstract refreshKakaoSession(refreshToken: string): Promise<KakaoSession>;
  /** Issues a member access token for a signed-up Kakao account. */
  abstract signIn(kakaoId: string): Promise<string>;
}
