import {
  BadGatewayException,
  Injectable,
  OnModuleDestroy,
} from '@nestjs/common';
import nodemailer, { Transporter } from 'nodemailer';
import { DataSource, IsNull } from 'typeorm';
import {
  Application,
  ApplicationResultLog,
  EmailAlarm,
  NotificationType,
} from '../applications/application.entity';
import { seoulIso } from '../common/dates';
import { apiId } from '../common/http';
import { Settings } from '../config/settings';

export function notificationType(
  documentPass: boolean,
  finalPass: boolean | null,
): NotificationType {
  if (documentPass && finalPass === null) return NotificationType.DOCUMENT_PASS;
  if (documentPass && finalPass === true) return NotificationType.FINAL_PASS;
  return NotificationType.FAIL;
}

function notificationMessage(type: NotificationType, name: string): string {
  switch (type) {
    case NotificationType.DOCUMENT_PASS:
      return `${name}님 서류 합격 축하드립니다. \n면접 일정은 추후 공지될 예정입니다. \n감사합니다.`;
    case NotificationType.FINAL_PASS:
      return `${name}님 최종 합격 축하드립니다. \n추후 일정은 문자로 공지될 예정입니다. \n감사합니다.`;
    case NotificationType.FAIL:
      return `${name}님 연세골프에 지원해주셔서 감사합니다. \n\n\n안타깝게도 ${name}님께 이번 연골 모집에서 합격의 소식을 전해드리지 못하게 되었습니다.${name}님의 뛰어난 열정에도 불구하고, 연세골프는 한정된 인원으로만 운영되는 만큼 아쉽게도 이런 소식을 전해드리게 됐습니다.비록 이번 모집에서 ${name}님과 함께하지 못하지만, 다음에 함께 할 수 있기를 바라겠습니다. \n\n바쁘신 와중에 지원해주셔서 감사합니다. \n\n연세 골프 운영진 드림`;
  }
}

@Injectable()
export class EmailService implements OnModuleDestroy {
  private readonly transport: Transporter;
  constructor(
    private readonly db: DataSource,
    private readonly settings: Settings,
  ) {
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
      throw new BadGatewayException(
        '이메일 전송에 실패했습니다. 다시 시도해주세요.',
        { cause: error },
      );
    }
  }
  async confirmation(email: string): Promise<void> {
    await this.send(
      email,
      '[연세골프] 지원서 이메일 확인',
      '안녕하세요 연세대학교 골프동아리입니다.\n\n지원서에 작성해주신 이메일 주소 확인을 위해 발송한 메일입니다.\n이 메일을 정상적으로 받으셨다면 입력하신 이메일 주소로 지원 결과가 안내됩니다.\n\n감사합니다.\n연세대학교 골프동아리 드림',
    );
  }
  async waiting(semester: number) {
    const alarms = await this.db
      .getRepository(EmailAlarm)
      .find({ where: { semester: String(semester) }, order: { id: 'ASC' } });
    return {
      emailAlarms: alarms.map((alarm) => ({
        id: apiId(alarm.id),
        email: alarm.email,
        semester: alarm.semester === null ? null : apiId(alarm.semester),
        sentAt: alarm.sentAt ? seoulIso(alarm.sentAt) : null,
      })),
    };
  }
  async sendRecruitmentAlerts(): Promise<void> {
    const alarms = await this.db
      .getRepository(EmailAlarm)
      .find({ where: { sentAt: IsNull() }, order: { id: 'ASC' } });
    for (const candidate of alarms) {
      await this.db.transaction(async (manager) => {
        const repository = manager.getRepository(EmailAlarm);
        const alarm = await repository.findOne({
          where: { id: candidate.id },
          lock: { mode: 'pessimistic_write' },
        });
        if (!alarm || alarm.sentAt) return;
        await this.send(
          alarm.email,
          '연세대학교 골프동아리입니다.',
          '연세대학교 골프동아리입니다. \n연세대학교 골프동아리 모집이 시작되었습니다.\n https://yonsei-golf.kr/apply 에서 확인해주세요',
        );
        alarm.sentAt = new Date();
        await repository.save(alarm);
      });
    }
  }
  async applicationNotification(
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
      const text =
        type === null
          ? `${application.name}님의 지원서가 정상적으로 제출되었습니다. \n\n서류 합격 여부는 추후 이메일로 공지될 예정입니다. \n\n감사합니다.`
          : notificationMessage(type, application.name);
      await this.send(
        application.email,
        type === null
          ? '안녕하세요. 연세골프입니다.'
          : '안녕하세요. 연세대학교 골프동아리 결과 메일입니다.',
        text,
      );
      await logs.save(
        logs.create({
          applicationId,
          notificationType: type,
          sentAt: new Date(),
        }),
      );
    });
  }
  onModuleDestroy(): void {
    this.transport.close();
  }
}
