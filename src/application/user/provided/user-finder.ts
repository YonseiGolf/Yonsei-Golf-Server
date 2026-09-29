import { PageResponse } from '../../shared/page';
import { UsersQuery } from './user-requests';
import { LeadersResponse, UserResponse } from './user-responses';

export abstract class UserFinder {
  abstract list(query: UsersQuery): Promise<PageResponse<UserResponse>>;
  abstract leaders(): Promise<LeadersResponse>;
}
