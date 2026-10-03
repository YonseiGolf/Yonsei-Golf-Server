import { Module } from '@nestjs/common';
import { MailTemplateService } from '../../application/mail/mail-template.service';
import { MailTemplateFinder } from '../../application/mail/provided/mail-template-finder';
import { MailTemplateRegister } from '../../application/mail/provided/mail-template-register';
import { MailTemplateRepository } from '../../application/mail/required/mail-template-repository';
import { MailTemplate } from '../../domain/mail/mail-template';
import { MailTemplateController } from '../webapi/mail/mail-template.controller';
import { repositoryProvider } from './repository';

@Module({
  controllers: [MailTemplateController],
  providers: [
    repositoryProvider(MailTemplateRepository, MailTemplate),
    MailTemplateService,
    { provide: MailTemplateFinder, useExisting: MailTemplateService },
    { provide: MailTemplateRegister, useExisting: MailTemplateService },
  ],
  // The apply and recruitment slices render their mails through this port.
  exports: [MailTemplateFinder],
})
export class MailTemplateModule {}
