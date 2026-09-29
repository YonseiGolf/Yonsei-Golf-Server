import { OnModuleDestroy } from '@nestjs/common';
import nodemailer, { Transporter } from 'nodemailer';
import { MailSender } from '../../../application/shared/mail-sender';
import { ExternalServiceError } from '../../../support/errors';
import { Adapter } from '../../../support/stereotype';
import { Settings } from '../../config/settings';

@Adapter()
export class SmtpMailSender implements MailSender, OnModuleDestroy {
  private readonly transport: Transporter;
  constructor(private readonly settings: Settings) {
    const mail = settings.mail;
    this.transport = nodemailer.createTransport({
      host: mail.host,
      port: mail.port,
      secure: mail.secure,
      requireTLS: mail.requireTLS,
      auth: mail.username
        ? { user: mail.username, pass: mail.password }
        : undefined,
      connectionTimeout: 10000,
      greetingTimeout: 10000,
      socketTimeout: 10000,
    });
  }
  async send(to: string, subject: string, text: string): Promise<void> {
    try {
      await this.transport.sendMail({
        from: this.settings.mail.from,
        to,
        subject,
        text,
      });
    } catch (error) {
      throw new ExternalServiceError(
        '이메일 전송에 실패했습니다. 다시 시도해주세요.',
        { cause: error },
      );
    }
  }
  onModuleDestroy(): void {
    this.transport.close();
  }
}
