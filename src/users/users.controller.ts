import {
  Body,
  Controller,
  Get,
  HttpCode,
  Param,
  Patch,
  Post,
  Query,
  Req,
  Res,
  UnauthorizedException,
} from '@nestjs/common';
import { Request, Response } from 'express';
import { IdPipe, success } from '../common/http';
import { Settings } from '../config/settings';
import { Access, AuthService, PrincipalId } from './auth';
import { KakaoClient } from './kakao.client';
import { KakaoCodeDto, SignUpDto, UserClassDto, UsersQuery } from './users.dto';
import { UsersService } from './users.service';

@Controller()
export class UsersController {
  constructor(
    private readonly users: UsersService,
    private readonly auth: AuthService,
    private readonly kakao: KakaoClient,
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

  @Post('oauth/kakao')
  @HttpCode(200)
  async oauth(
    @Body() dto: KakaoCodeDto,
    @Res({ passthrough: true }) response: Response,
  ) {
    const result = await this.kakao.exchange(dto.kakaoCode);
    if (result.refreshToken)
      response.cookie('refreshToken', result.refreshToken, {
        ...this.cookieOptions(),
        maxAge: 14 * 24 * 3600 * 1000,
      });
    return success('카카오 로그인 성공', {
      accessToken: this.auth.oauthToken(result.id),
    });
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
      accessToken: this.auth.accessToken(await this.users.findByKakaoId(id)),
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
    const result = await this.kakao.refresh(token);
    if (result.refreshToken)
      response.cookie('refreshToken', result.refreshToken, {
        ...this.cookieOptions(),
        maxAge: 14 * 24 * 3600 * 1000,
      });
    return success('토큰 재발급 성공', {
      accessToken: this.auth.accessToken(
        await this.users.findByKakaoId(result.id),
      ),
    });
  }
  @Post('users/logout')
  @HttpCode(200)
  @Access('user')
  logout(@Res({ passthrough: true }) response: Response) {
    response.clearCookie('refreshToken', this.cookieOptions());
    return success('로그아웃 성공');
  }
  @Get('admin/users')
  @Access('admin')
  async list(@Query() query: UsersQuery) {
    return success('회원 조회 성공', await this.users.list(query));
  }

  @Patch('admin/users/:userId')
  @Access('admin')
  async update(@Param('userId', IdPipe) id: string, @Body() dto: UserClassDto) {
    await this.users.changeClass(id, dto.userClass);
    return success('회원 등급 변경 성공');
  }
  @Get('users/leaders')
  async leaders() {
    return success('운영진 조회 성공', await this.users.leaders());
  }
}
