import { DataSource, IsNull } from 'typeorm';
import { EmailAlarm } from '../../domain/recruitment/email-alarm';
import { ApplicationService } from '../../support/stereotype';
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
    for (const candidate of candidates) {
      await this.db.transaction(async (manager) => {
        const repository = manager.getRepository(EmailAlarm);
        const alarm = await repository.findOne({
          where: { id: candidate.id },
          lock: { mode: 'pessimistic_write' },
        });
        if (!alarm || alarm.isSent()) return;
        await this.mail.send(
          alarm.email,
          '연세대학교 골프동아리입니다.',
          '연세대학교 골프동아리입니다. \n연세대학교 골프동아리 모집이 시작되었습니다.\n https://yonsei-golf.kr/apply 에서 확인해주세요',
        );
        alarm.markSent(new Date());
        await repository.save(alarm);
      });
    }
  }
}
