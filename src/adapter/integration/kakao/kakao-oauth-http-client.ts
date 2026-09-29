import {
  KakaoAccount,
  KakaoOAuthClient,
} from '../../../application/user/required/kakao-oauth-client';
import {
  ExternalServiceError,
  UnauthenticatedError,
} from '../../../support/errors';
import { Adapter } from '../../../support/stereotype';
import { Settings } from '../../config/settings';

interface KakaoTokens {
  access_token: string;
  refresh_token?: string;
}

// Kakao's error fields (e.g. "invalid_grant KOE320 authorization code not found") for the logs.
async function kakaoError(api: string, response: globalThis.Response) {
  const text = (await response.text().catch(() => '')).slice(0, 500);
  let detail = text;
  try {
    const body = JSON.parse(text) as Record<string, unknown>;
    detail = ['error', 'error_code', 'error_description', 'code', 'msg']
      .map((key) => body[key])
      .filter((value) => value !== undefined)
      .join(' ');
  } catch {
    // Not JSON: keep the raw text.
  }
  return `Kakao ${api} ${response.status}${detail ? `: ${detail}` : ''}`;
}

@Adapter()
export class KakaoOAuthHttpClient implements KakaoOAuthClient {
  constructor(private readonly settings: Settings) {}
  private async tokens(
    parameters: Record<string, string>,
  ): Promise<KakaoTokens> {
    let response: globalThis.Response;
    try {
      response = await fetch(this.settings.kakao.tokenUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8',
        },
        body: new URLSearchParams({
          client_id: this.settings.kakao.clientId,
          client_secret: this.settings.kakao.clientSecret,
          ...parameters,
        }),
        signal: AbortSignal.timeout(10000),
      });
    } catch (error) {
      throw new ExternalServiceError('카카오 인증 서버에 연결할 수 없습니다.', {
        cause: error,
      });
    }
    if (!response.ok)
      throw new UnauthenticatedError('카카오 인증에 실패했습니다.', {
        cause: await kakaoError('token', response),
      });
    const result: unknown = await response.json();
    if (
      !result ||
      typeof result !== 'object' ||
      !('access_token' in result) ||
      typeof result.access_token !== 'string'
    )
      throw new ExternalServiceError('카카오 응답이 올바르지 않습니다.');
    return result as KakaoTokens;
  }
  private async userId(accessToken: string): Promise<string> {
    let response: globalThis.Response;
    try {
      response = await fetch(this.settings.kakao.userUrl, {
        headers: { Authorization: `Bearer ${accessToken}` },
        signal: AbortSignal.timeout(10000),
      });
    } catch (error) {
      throw new ExternalServiceError(
        '카카오 사용자 정보를 조회할 수 없습니다.',
        {
          cause: error,
        },
      );
    }
    if (!response.ok)
      throw new UnauthenticatedError('카카오 사용자 인증에 실패했습니다.', {
        cause: await kakaoError('user', response),
      });
    const result = (await response.json()) as { id?: unknown };
    const id = result.id;
    if (
      (typeof id !== 'number' && typeof id !== 'string') ||
      !/^[1-9]\d*$/.test(String(id)) ||
      (typeof id === 'number' && !Number.isSafeInteger(id))
    )
      throw new ExternalServiceError('카카오 사용자 ID가 올바르지 않습니다.');
    return String(id);
  }
  async exchange(code: string): Promise<KakaoAccount> {
    const callback = this.settings.kakao.callbackUrl;
    const tokens = await this.tokens({
      grant_type: 'authorization_code',
      code,
      ...(callback ? { redirect_uri: callback } : {}),
    });
    return {
      id: await this.userId(tokens.access_token),
      refreshToken: tokens.refresh_token,
    };
  }
  async refresh(token: string): Promise<KakaoAccount> {
    const tokens = await this.tokens({
      grant_type: 'refresh_token',
      refresh_token: token,
    });
    return {
      id: await this.userId(tokens.access_token),
      refreshToken: tokens.refresh_token,
    };
  }
}
