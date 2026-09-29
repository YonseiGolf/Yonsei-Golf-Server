import { DataSource, IsNull } from 'typeorm';
import { Application } from '../../domain/apply/application';
import {
  ApplicationResultLog,
  NotificationType,
  notificationTypeOf,
} from '../../domain/apply/application-result-log';
import { ApplicationService } from '../../support/stereotype';
import { MailSender } from '../shared/mail-sender';
import { applicationMail, confirmationMail } from './application-mails';
import { ApplicationQueryService } from './application-query.service';
import { ApplicationNotifier } from './provided/application-notifier';
import { ResultDto } from './provided/apply-requests';

@ApplicationService()
export class ApplicationNotificationService implements ApplicationNotifier {
  constructor(
    private readonly db: DataSource,
    private readonly mail: MailSender,
    private readonly query: ApplicationQueryService,
  ) {}

  async confirmEmail(email: string): Promise<void> {
    await this.mail.send(
      email,
      confirmationMail.subject,
      confirmationMail.text,
    );
  }
  async notify(
    applicationId: string,
    type: NotificationType | null,
  ): Promise<void> {
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
      const mail = applicationMail(type, application.name);
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
