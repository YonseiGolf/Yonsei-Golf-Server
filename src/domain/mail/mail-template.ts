import { Column, Entity } from 'typeorm';
import { InvalidInputError } from '../../support/errors';
import { BaseEntity } from '../common/base-entity';

export enum MailTemplateType {
  EMAIL_CONFIRMATION = 'EMAIL_CONFIRMATION',
  APPLICATION_RECEIPT = 'APPLICATION_RECEIPT',
  DOCUMENT_PASS = 'DOCUMENT_PASS',
  FINAL_PASS = 'FINAL_PASS',
  FAIL = 'FAIL',
  RECRUITMENT_START = 'RECRUITMENT_START',
}

export interface Mail {
  subject: string;
  text: string;
}

/** Replaced with the applicant's name in the mails sent to an applicant. */
export const NAME_PLACEHOLDER = '{{이름}}';
const PLACEHOLDER = /\{\{[^{}]*\}\}/g;

/** The placeholders a template of this type may use. */
export function placeholdersOf(type: MailTemplateType): string[] {
  return type === MailTemplateType.EMAIL_CONFIRMATION ||
    type === MailTemplateType.RECRUITMENT_START
    ? []
    : [NAME_PLACEHOLDER];
}

/** The subject and body sent for one kind of mail. */
@Entity('mail_template')
export class MailTemplate extends BaseEntity {
  @Column({ type: 'varchar', length: 64 }) type!: MailTemplateType;
  @Column({ type: 'varchar', length: 255 }) subject!: string;
  @Column({ type: 'text' }) body!: string;

  static write(
    type: MailTemplateType,
    subject: string,
    body: string,
  ): MailTemplate {
    const template = new MailTemplate();
    template.type = type;
    template.rewrite(subject, body);
    return template;
  }

  /** Rejects a placeholder the type cannot fill, so it is never mailed as is. */
  rewrite(subject: string, body: string): void {
    if (!subject.trim() || !body.trim())
      throw new InvalidInputError('메일 제목과 본문을 입력해주세요.');
    if (/[\r\n]/.test(subject))
      throw new InvalidInputError('메일 제목은 한 줄로 입력해주세요.');
    const allowed = placeholdersOf(this.type);
    for (const [placeholder] of `${subject}\n${body}`.matchAll(PLACEHOLDER))
      if (!allowed.includes(placeholder))
        throw new InvalidInputError(
          `이 메일에서 쓸 수 없는 변수입니다: ${placeholder}`,
        );
    this.subject = subject;
    this.body = body;
  }

  render(name = ''): Mail {
    // A replacer function keeps `$&`-style patterns in a name literal.
    const fill = (value: string) =>
      value.replaceAll(NAME_PLACEHOLDER, () => name);
    return { subject: fill(this.subject), text: fill(this.body) };
  }
}
