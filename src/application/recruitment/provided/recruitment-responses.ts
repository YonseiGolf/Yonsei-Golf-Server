export interface RecruitmentResponse {
  id: number | string;
  semester: number | null;
  startDate: string | null;
  endDate: string | null;
  firstResultDate: string | null;
  finalResultDate: string | null;
  interviewStartDate: string | null;
  interviewEndDate: string | null;
  orientationDate: string | null;
}

export interface InterviewTimeResponse {
  id: number | string;
  /** `YYYY-MM-DD HH:mm` in Korean time. */
  interviewDateTime: string | null;
}

export interface EmailAlarmResponse {
  id: number | string;
  email: string;
  semester: number | string | null;
  sentAt: string | null;
}
