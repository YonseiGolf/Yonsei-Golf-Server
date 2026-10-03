import { MailTemplateType } from '../../domain/mail/mail-template';

/** The wording sent until an admin saves another one. */
export const defaultMailTemplates: Record<
  MailTemplateType,
  { subject: string; body: string }
> = {
  [MailTemplateType.EMAIL_CONFIRMATION]: {
    subject: '[연세골프] 지원서 이메일 확인',
    body: '안녕하세요 연세대학교 골프동아리입니다.\n\n지원서에 작성해주신 이메일 주소 확인을 위해 발송한 메일입니다.\n이 메일을 정상적으로 받으셨다면 입력하신 이메일 주소로 지원 결과가 안내됩니다.\n\n감사합니다.\n연세대학교 골프동아리 드림',
  },
  [MailTemplateType.APPLICATION_RECEIPT]: {
    subject: '안녕하세요. 연세골프입니다.',
    body: '{{이름}}님의 지원서가 정상적으로 제출되었습니다. \n\n서류 합격 여부는 추후 이메일로 공지될 예정입니다. \n\n감사합니다.',
  },
  [MailTemplateType.DOCUMENT_PASS]: {
    subject: '안녕하세요. 연세대학교 골프동아리 결과 메일입니다.',
    body: '{{이름}}님 서류 합격 축하드립니다. \n면접 일정은 추후 공지될 예정입니다. \n감사합니다.',
  },
  [MailTemplateType.FINAL_PASS]: {
    subject: '안녕하세요. 연세대학교 골프동아리 결과 메일입니다.',
    body: '{{이름}}님 최종 합격 축하드립니다. \n추후 일정은 문자로 공지될 예정입니다. \n감사합니다.',
  },
  [MailTemplateType.FAIL]: {
    subject: '안녕하세요. 연세대학교 골프동아리 결과 메일입니다.',
    body: '{{이름}}님 연세골프에 지원해주셔서 감사합니다. \n\n\n안타깝게도 {{이름}}님께 이번 연골 모집에서 합격의 소식을 전해드리지 못하게 되었습니다.{{이름}}님의 뛰어난 열정에도 불구하고, 연세골프는 한정된 인원으로만 운영되는 만큼 아쉽게도 이런 소식을 전해드리게 됐습니다.비록 이번 모집에서 {{이름}}님과 함께하지 못하지만, 다음에 함께 할 수 있기를 바라겠습니다. \n\n바쁘신 와중에 지원해주셔서 감사합니다. \n\n연세 골프 운영진 드림',
  },
  [MailTemplateType.RECRUITMENT_START]: {
    subject: '연세대학교 골프동아리입니다.',
    body: '연세대학교 골프동아리입니다. \n연세대학교 골프동아리 모집이 시작되었습니다.\n https://yonsei-golf.kr/apply 에서 확인해주세요',
  },
};
