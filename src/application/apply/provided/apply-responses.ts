import { InterviewTimeResponse } from '../../recruitment/provided/recruitment-responses';

export interface ApplicationSummaryResponse {
  id: number | string;
  photo: string | null;
  name: string;
  interviewTime: string | null;
  documentPass: boolean | null;
  finalPass: boolean | null;
}

export interface ActivityResponse {
  clubName: string;
  startDate: string | null;
  endDate: string | null;
  role: string | null;
}

/** Every stored field except `photoKey`, which is resolved into `photo`. */
export interface ApplicationDetailResponse {
  id: number | string;
  name: string;
  photo: string | null;
  birthDate: string | null;
  studentId: number | string;
  email: string | null;
  major: string;
  phoneNumber: string;
  selfIntroduction: string;
  applyReason: string;
  skillEvaluation: string;
  golfMemory: string;
  swingVideo: string;
  submitTime: string | null;
  documentPass: boolean | null;
  finalPass: boolean | null;
  interviewTime: string | null;
  semester: number | string | null;
  activities: ActivityResponse[];
  availableInterviewTimes: InterviewTimeResponse[];
}

export interface PhotoUploadResponse {
  uploadUrl: string;
  imageKey: string;
  uploadHeaders: Record<string, string>;
}
