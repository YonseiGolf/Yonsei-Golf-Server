import { User, UserRole } from '../../domain/user/user';
import { NotFoundError } from '../../support/errors';
import { ApplicationService } from '../../support/stereotype';
import { apiId } from '../shared/api-id';
import { PageResponse, pageResponse } from '../shared/page';
import { UserFinder } from './provided/user-finder';
import { UsersQuery } from './provided/user-requests';
import {
  LeaderResponse,
  LeadersResponse,
  UserResponse,
} from './provided/user-responses';
import { UserRepository } from './required/user-repository';

@ApplicationService()
export class UserQueryService implements UserFinder {
  constructor(private readonly users: UserRepository) {}

  async getById(id: string): Promise<User> {
    const user = await this.users.findOneBy({ id });
    if (!user) throw new NotFoundError('존재하지 않는 유저입니다.');
    return user;
  }
  async getByKakaoId(kakaoId: string): Promise<User> {
    const user = await this.users.findOneBy({ kakaoId });
    if (!user) throw new NotFoundError('존재하지 않는 유저입니다.');
    return user;
  }
  async list(query: UsersQuery): Promise<PageResponse<UserResponse>> {
    const [users, total] = await this.users.findAndCount({
      where: query.userClass ? { userClass: query.userClass } : {},
      order: { semester: 'ASC', name: 'ASC', id: 'ASC' },
      skip: query.page * query.size,
      take: query.size,
    });
    return pageResponse(
      users.map((user) => ({
        ...user,
        id: apiId(user.id),
        kakaoId: apiId(user.kakaoId),
      })),
      total,
      query,
    );
  }
  async leaders(): Promise<LeadersResponse> {
    const leader = await this.users.findOneBy({ role: UserRole.LEADER });
    const assistants = await this.users.findBy({
      role: UserRole.ASSISTANT_LEADER,
    });
    const response = (user: User): LeaderResponse => ({
      name: user.name,
      phoneNumber: user.phoneNumber,
      role: user.role,
    });
    return {
      leader: leader ? response(leader) : null,
      assistantLeaders: assistants.map(response),
    };
  }
}
