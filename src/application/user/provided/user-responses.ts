import { UserClass, UserRole } from '../../../domain/user/user';

export interface UserResponse {
  id: number | string;
  kakaoId: number | string;
  name: string;
  phoneNumber: string;
  studentId: number;
  major: string;
  semester: number;
  role: UserRole;
  userClass: UserClass;
}

export interface LeaderResponse {
  name: string;
  phoneNumber: string;
  role: UserRole;
}

export interface LeadersResponse {
  leader: LeaderResponse | null;
  assistantLeaders: LeaderResponse[];
}
