import {
  Body,
  HttpCode,
  Post,
  Req,
  Res,
  UnauthorizedException,
} from '@nestjs/common';
import { Request, Response } from 'express';
import { LoginManager } from '../../../application/user/provided/login-manager';
import { UserRegister } from '../../../application/user/provided/user-register';
import {
  KakaoCodeDto,
  SignUpDto,
} from '../../../application/user/provided/user-requests';
import { WebApiAdapter } from '../../../support/stereotype';
import { Settings } from '../../config/settings';
import { success } from '../api-response';
import { Access, PrincipalId } from '../security/access';

@WebApiAdapter()
export class AuthController {
  constructor(
    private readonly login: LoginManager,
    private readonly users: UserRegister,
    private readonly settings: Settings,
  ) {}

  private cookieOptions() {
    return {
      httpOnly: true,
      secure: this.settings.cookieSecure,
      sameSite: this.settings.cookieSecure
        ? ('none' as const)
        : ('lax' as const),
      path: '/',
    };
  }
  private setRefreshCookie(response: Response, token: string | undefined) {
    if (token)
      response.cookie('refreshToken', token, {
        ...this.cookieOptions(),
        maxAge: 14 * 24 * 3600 * 1000,
      });
  }

  @Post('oauth/kakao')
  @HttpCode(200)
  async oauth(
    @Body() dto: KakaoCodeDto,
    @Res({ passthrough: true }) response: Response,
  ) {
    const result = await this.login.loginWithKakao(dto.kakaoCode);
    this.setRefreshCookie(response, result.kakaoRefreshToken);
    return success('카카오 로그인 성공', { accessToken: result.accessToken });
  }
  @Post('users/signUp')
  @HttpCode(200)
  @Access('oauth')
  async signUp(@Body() dto: SignUpDto, @PrincipalId() id: string) {
    await this.users.signUp(dto, id);
    return success('회원가입 성공');
  }
  @Post('users/signIn')
  @HttpCode(200)
  @Access('oauth')
  async signIn(@PrincipalId() id: string) {
    return success('로그인 성공', {
      accessToken: await this.login.signIn(id),
    });
  }
  @Post('users/loggedIn')
  @HttpCode(200)
  @Access('user')
  loggedIn() {
    return success('로그인 상태입니다.');
  }

  @Post('users/signIn/refresh')
  @HttpCode(200)
  async refresh(
    @Req() request: Request,
    @Res({ passthrough: true }) response: Response,
  ) {
    const token = (request.cookies as Record<string, unknown> | undefined)
      ?.refreshToken;
    if (typeof token !== 'string' || !token)
      throw new UnauthorizedException('Refresh Token이 존재하지 않습니다.');
    const session = await this.login.refreshKakaoSession(token);
    // A rotated Kakao token is kept even when the member lookup below fails.
    this.setRefreshCookie(response, session.kakaoRefreshToken);
    return success('토큰 재발급 성공', {
      accessToken: await this.login.signIn(session.kakaoId),
    });
  }
  @Post('users/logout')
  @HttpCode(200)
  @Access('user')
  logout(@Res({ passthrough: true }) response: Response) {
    response.clearCookie('refreshToken', this.cookieOptions());
    return success('로그아웃 성공');
  }
}
