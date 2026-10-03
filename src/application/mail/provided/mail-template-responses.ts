import { MailTemplateType } from '../../../domain/mail/mail-template';

export interface MailTemplateResponse {
  type: MailTemplateType;
  subject: string;
  body: string;
  /** Written as is in the subject or body, e.g. `{{이름}}`. */
  placeholders: string[];
  /** False while the default wording is sent. */
  customized: boolean;
}
