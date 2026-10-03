import { DataSource, IsNull } from 'typeorm';
import { Application } from '../../domain/apply/application';
import {
  ApplicationResultLog,
  NotificationType,
  notificationTypeOf,
} from '../../domain/apply/application-result-log';
import { MailTemplateType } from '../../domain/mail/mail-template';
import { ApplicationService } from '../../support/stereotype';
import { MailTemplateFinder } from '../mail/provided/mail-template-finder';
import { MailSender } from '../shared/mail-sender';
import { ApplicationQueryService } from './application-query.service';
import { ApplicationNotifier } from './provided/application-notifier';
import { ResultDto } from './provided/apply-requests';

const resultTemplates: Record<NotificationType, MailTemplateType> = {
  [NotificationType.DOCUMENT_PASS]: MailTemplateType.DOCUMENT_PASS,
  [NotificationType.DOCUMENT_FAIL]: MailTemplateType.DOCUMENT_FAIL,
  [NotificationType.FINAL_PASS]: MailTemplateType.FINAL_PASS,
  [NotificationType.FINAL_FAIL]: MailTemplateType.FINAL_FAIL,
};

@ApplicationService()
export class ApplicationNotificationService implements ApplicationNotifier {
  constructor(
    private readonly db: DataSource,
    private readonly mail: MailSender,
    private readonly templates: MailTemplateFinder,
    private readonly query: ApplicationQueryService,
  ) {}

  async confirmEmail(email: string): Promise<void> {
    const mail = (
      await this.templates.find(MailTemplateType.EMAIL_CONFIRMATION)
    ).render();
    await this.mail.send(email, mail.subject, mail.text);
  }
  async notify(
    applicationId: string,
    type: NotificationType | null,
  ): Promise<void> {
    // Read outside the transaction, which would otherwise wait on a second pooled connection.
    const template = await this.templates.find(
      type === null
        ? MailTemplateType.APPLICATION_RECEIPT
        : resultTemplates[type],
    );
    // Serialize concurrent sends for the same application. SMTP is not transactional:
    // a crash after SMTP acceptance but before commit can still cause a duplicate on retry.
    await this.db.transaction(async (manager) => {
      const application = await manager.findOne(Application, {
        where: { id: applicationId },
        lock: { mode: 'pessimistic_write' },
      });
      if (!application?.email) return;
      const logs = manager.getRepository(ApplicationResultLog);
      if (
        await logs.existsBy({
          applicationId,
          notificationType: type ?? IsNull(),
        })
      )
        return;
      const mail = template.render(application.name);
      await this.mail.send(application.email, mail.subject, mail.text);
      await logs.save(
        ApplicationResultLog.record(applicationId, type, new Date()),
      );
    });
  }
  async notifyResults(request: ResultDto): Promise<void> {
    const applications = await this.query.findByDecision(
      request.documentPass,
      request.finalPass,
    );
    const type = notificationTypeOf(request.documentPass, request.finalPass);
    for (const application of applications)
      await this.notify(application.id, type);
  }
}
