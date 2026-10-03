import { InterviewTimeResponse } from '../../recruitment/provided/recruitment-responses';

export interface ApplicationSummaryResponse {
  id: number | string;
  photo: string | null;
  name: string;
  interviewTime: string | null;
  documentPass: boolean | null;
  finalPass: boolean | null;
  /**
   * When the mail for the current decisions was sent (`YYYY-MM-DD HH:mm`, Korean time):
   * the receipt while undecided, else that result stage's mail. Null if it was not sent.
   */
  mailSentAt: string | null;
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
