import { Global, Module } from '@nestjs/common';
import { JwtModule } from '@nestjs/jwt';
import { AuthService } from '../../application/user/auth.service';
import { AccessVerifier } from '../../application/user/provided/access-verifier';
import { LoginManager } from '../../application/user/provided/login-manager';
import { UserFinder } from '../../application/user/provided/user-finder';
import { UserRegister } from '../../application/user/provided/user-register';
import { KakaoOAuthClient } from '../../application/user/required/kakao-oauth-client';
import { TokenIssuer } from '../../application/user/required/token-issuer';
import { UserRepository } from '../../application/user/required/user-repository';
import { UserService } from '../../application/user/user.service';
import { UserQueryService } from '../../application/user/user-query.service';
import { User } from '../../domain/user/user';
import { KakaoOAuthHttpClient } from '../integration/kakao/kakao-oauth-http-client';
import { JwtTokenIssuer } from '../security/jwt/jwt-token-issuer';
import { AuthController } from '../webapi/auth/auth.controller';
import { AccessGuard } from '../webapi/security/access';
import { UserController } from '../webapi/user/user.controller';
import { repositoryProvider } from './repository';

// Global so every controller's AccessGuard can resolve AccessVerifier.
@Global()
@Module({
  imports: [JwtModule.register({})],
  controllers: [AuthController, UserController],
  providers: [
    repositoryProvider(UserRepository, User),
    { provide: TokenIssuer, useClass: JwtTokenIssuer },
    { provide: KakaoOAuthClient, useClass: KakaoOAuthHttpClient },
    UserQueryService,
    UserService,
    AuthService,
    { provide: UserFinder, useExisting: UserQueryService },
    { provide: UserRegister, useExisting: UserService },
    { provide: LoginManager, useExisting: AuthService },
    { provide: AccessVerifier, useExisting: AuthService },
    AccessGuard,
  ],
  exports: [AccessVerifier, AccessGuard],
})
export class UserModule {}
