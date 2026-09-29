import { Body, Get, Param, Patch, Query } from '@nestjs/common';
import { UserFinder } from '../../../application/user/provided/user-finder';
import { UserRegister } from '../../../application/user/provided/user-register';
import {
  UserClassDto,
  UsersQuery,
} from '../../../application/user/provided/user-requests';
import { WebApiAdapter } from '../../../support/stereotype';
import { success } from '../api-response';
import { IdPipe } from '../id.pipe';
import { Access } from '../security/access';

@WebApiAdapter()
export class UserController {
  constructor(
    private readonly finder: UserFinder,
    private readonly register: UserRegister,
  ) {}

  @Get('admin/users')
  @Access('admin')
  async list(@Query() query: UsersQuery) {
    return success('회원 조회 성공', await this.finder.list(query));
  }
  @Patch('admin/users/:userId')
  @Access('admin')
  async update(@Param('userId', IdPipe) id: string, @Body() dto: UserClassDto) {
    await this.register.changeClass(id, dto.userClass);
    return success('회원 등급 변경 성공');
  }
  @Get('users/leaders')
  async leaders() {
    return success('운영진 조회 성공', await this.finder.leaders());
  }
}
