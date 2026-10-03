import { DataSource, IsNull } from 'typeorm';
import { MailTemplateType } from '../../domain/mail/mail-template';
import { EmailAlarm } from '../../domain/recruitment/email-alarm';
import { ApplicationService } from '../../support/stereotype';
import { MailTemplateFinder } from '../mail/provided/mail-template-finder';
import { apiId } from '../shared/api-id';
import { seoulIso } from '../shared/dates';
import { MailSender } from '../shared/mail-sender';
import { RecruitmentAlertManager } from './provided/recruitment-alert-manager';
import { EmailAlarmDto } from './provided/recruitment-requests';
import { EmailAlarmResponse } from './provided/recruitment-responses';
import { EmailAlarmRepository } from './required/email-alarm-repository';

@ApplicationService()
export class RecruitmentAlertService implements RecruitmentAlertManager {
  constructor(
    private readonly db: DataSource,
    private readonly alarms: EmailAlarmRepository,
    private readonly mail: MailSender,
    private readonly templates: MailTemplateFinder,
  ) {}

  async subscribe(request: EmailAlarmDto): Promise<void> {
    await this.alarms.save(
      EmailAlarm.subscribe(request.email, request.semester),
    );
  }
  async waiting(
    semester: number,
  ): Promise<{ emailAlarms: EmailAlarmResponse[] }> {
    const alarms = await this.alarms.find({
      where: { semester: String(semester) },
      order: { id: 'ASC' },
    });
    return {
      emailAlarms: alarms.map((alarm) => ({
        id: apiId(alarm.id),
        email: alarm.email,
        semester: alarm.semester === null ? null : apiId(alarm.semester),
        sentAt: alarm.sentAt ? seoulIso(alarm.sentAt) : null,
      })),
    };
  }
  async sendAll(): Promise<void> {
    const candidates = await this.alarms.find({
      where: { sentAt: IsNull() },
      order: { id: 'ASC' },
    });
    const mail = (
      await this.templates.find(MailTemplateType.RECRUITMENT_START)
    ).render();
    for (const candidate of candidates) {
      await this.db.transaction(async (manager) => {
        const repository = manager.getRepository(EmailAlarm);
        const alarm = await repository.findOne({
          where: { id: candidate.id },
          lock: { mode: 'pessimistic_write' },
        });
        if (!alarm || alarm.isSent()) return;
        await this.mail.send(alarm.email, mail.subject, mail.text);
        alarm.markSent(new Date());
        await repository.save(alarm);
      });
    }
  }
}
