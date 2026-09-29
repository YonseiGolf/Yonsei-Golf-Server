import { Repository } from 'typeorm';
import { EmailAlarm } from '../../../domain/recruitment/email-alarm';

export abstract class EmailAlarmRepository extends Repository<EmailAlarm> {}
