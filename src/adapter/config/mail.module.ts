import { Module } from '@nestjs/common';
import { MailSender } from '../../application/shared/mail-sender';
import { SmtpMailSender } from '../integration/mail/smtp-mail-sender';

// One SMTP transport shared by every slice that sends mail.
@Module({
  providers: [{ provide: MailSender, useClass: SmtpMailSender }],
  exports: [MailSender],
})
export class MailModule {}
