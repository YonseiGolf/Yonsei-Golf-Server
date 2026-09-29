/**
 * Outbound mail, implemented by the SMTP adapter. Shared because the apply
 * and recruitment slices both send mail. A failure throws ExternalServiceError.
 */
export abstract class MailSender {
  abstract send(to: string, subject: string, text: string): Promise<void>;
}
