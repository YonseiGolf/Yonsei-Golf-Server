import { UserClass } from '../../../domain/user/user';
import { SignUpDto } from './user-requests';

export abstract class UserRegister {
  /** Creates the member, or links a member registered by name and student ID. */
  abstract signUp(request: SignUpDto, kakaoId: string): Promise<void>;
  abstract changeClass(userId: string, userClass: UserClass): Promise<void>;
}
