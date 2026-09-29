import { Abstract, Provider } from '@nestjs/common';
import { DataSource, EntityTarget, ObjectLiteral, Repository } from 'typeorm';

/** Binds a repository port declared in `application/<slice>/required` to TypeORM. */
export function repositoryProvider<T extends ObjectLiteral>(
  port: Abstract<Repository<T>>,
  entity: EntityTarget<T>,
): Provider {
  return {
    provide: port,
    useFactory: (db: DataSource) => db.getRepository(entity),
    inject: [DataSource],
  };
}
