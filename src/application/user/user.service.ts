import { User, UserClass } from '../../domain/user/user';
import { ConflictError } from '../../support/errors';
import { ApplicationService } from '../../support/stereotype';
import { UserRegister } from './provided/user-register';
import { SignUpDto } from './provided/user-requests';
import { UserRepository } from './required/user-repository';
import { UserQueryService } from './user-query.service';

@ApplicationService()
export class UserService implements UserRegister {
  constructor(
    private readonly users: UserRepository,
    private readonly query: UserQueryService,
  ) {}

  async signUp(request: SignUpDto, kakaoId: string): Promise<void> {
    await this.users.manager.transaction(async (manager) => {
      const repository = manager.getRepository(User);
      const linked = await repository.findOneBy({ kakaoId });
      const existing = await repository.findOneBy({
        name: request.name,
        studentId: request.studentId,
      });
      if (linked && (!existing || String(linked.id) !== String(existing.id)))
        throw new ConflictError('이미 가입된 카카오 계정입니다.');
      if (!existing) {
        await repository.save(User.register(request, kakaoId));
        return;
      }
      existing.linkKakao(request, kakaoId);
      await repository.save(existing);
    });
  }
  async changeClass(userId: string, userClass: UserClass): Promise<void> {
    const user = await this.query.getById(userId);
    user.changeClass(userClass);
    await this.users.save(user);
  }
}
