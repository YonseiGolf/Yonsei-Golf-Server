import {
  ConflictException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { apiId, pageResponse } from '../common/http';
import { User, UserClass, UserRole } from './user.entity';
import { SignUpDto, UsersQuery } from './users.dto';

@Injectable()
export class UsersService {
  constructor(
    @InjectRepository(User) private readonly users: Repository<User>,
  ) {}

  async findById(id: string): Promise<User> {
    const user = await this.users.findOneBy({ id });
    if (!user) throw new NotFoundException('존재하지 않는 유저입니다.');
    return user;
  }
  async findByKakaoId(kakaoId: string): Promise<User> {
    const user = await this.users.findOneBy({ kakaoId });
    if (!user) throw new NotFoundException('존재하지 않는 유저입니다.');
    return user;
  }
  async signUp(dto: SignUpDto, kakaoId: string): Promise<void> {
    await this.users.manager.transaction(async (manager) => {
      const repository = manager.getRepository(User);
      const linked = await repository.findOneBy({ kakaoId });
      const existing = await repository.findOneBy({
        name: dto.name,
        studentId: dto.studentId,
      });
      if (linked && (!existing || String(linked.id) !== String(existing.id)))
        throw new ConflictException('이미 가입된 카카오 계정입니다.');
      if (existing && existing.kakaoId !== '0' && existing.kakaoId !== kakaoId)
        throw new ConflictException(
          '이미 다른 카카오 계정에 연결된 회원입니다.',
        );
      await repository.save(
        repository.create({
          ...existing,
          ...dto,
          kakaoId,
          role: existing?.role ?? UserRole.MEMBER,
          userClass: existing?.userClass ?? UserClass.NONE,
        }),
      );
    });
  }
  async changeClass(id: string, userClass: UserClass): Promise<void> {
    const user = await this.findById(id);
    user.userClass = userClass;
    await this.users.save(user);
  }
  async list(query: UsersQuery) {
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
  async leaders() {
    const leader = await this.users.findOneBy({ role: UserRole.LEADER });
    const assistants = await this.users.findBy({
      role: UserRole.ASSISTANT_LEADER,
    });
    const response = (user: User) => ({
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
