import {
  MailTemplate,
  MailTemplateType,
} from '../../../domain/mail/mail-template';
import { MailTemplateResponse } from './mail-template-responses';

/** The wording an admin saved for each kind of mail, or else the default. */
export abstract class MailTemplateFinder {
  /** Every type, in the order the admin page lists them. */
  abstract list(): Promise<MailTemplateResponse[]>;
  /** The template to render a mail of this type with. */
  abstract find(type: MailTemplateType): Promise<MailTemplate>;
}
