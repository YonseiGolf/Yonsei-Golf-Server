import { MailTemplateType } from '../../../domain/mail/mail-template';
import { MailTemplateDto } from './mail-template-requests';

export abstract class MailTemplateRegister {
  abstract update(
    type: MailTemplateType,
    request: MailTemplateDto,
  ): Promise<void>;
  /** Removes the saved wording so the default is sent again. */
  abstract reset(type: MailTemplateType): Promise<void>;
}
