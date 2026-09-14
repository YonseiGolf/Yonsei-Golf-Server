import { Global, Module } from '@nestjs/common';
import { JwtModule } from '@nestjs/jwt';
import { TypeOrmModule } from '@nestjs/typeorm';
import { AuthGuard, AuthService } from './auth';
import { KakaoClient } from './kakao.client';
import { User } from './user.entity';
import { UsersController } from './users.controller';
import { UsersService } from './users.service';

@Global()
@Module({
  imports: [TypeOrmModule.forFeature([User]), JwtModule.register({})],
  controllers: [UsersController],
  providers: [UsersService, AuthService, AuthGuard, KakaoClient],
  exports: [AuthService, AuthGuard, UsersService],
})
export class UsersModule {}
