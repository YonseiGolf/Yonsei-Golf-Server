import { Repository } from 'typeorm';
import { User } from '../../../domain/user/user';

// Like a Spring Data repository, the TypeORM repository itself is the port.
// adapter/config binds this token to DataSource.getRepository(User).
export abstract class UserRepository extends Repository<User> {}
