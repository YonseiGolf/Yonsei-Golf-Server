import { DataSourceOptions } from 'typeorm';
import {
  Application,
  ApplicationInterviewTime,
  ClubActivity,
} from '../../domain/apply/application';
import { ApplicationResultLog } from '../../domain/apply/application-result-log';
import { EmailAlarm } from '../../domain/recruitment/email-alarm';
import {
  InterviewTime,
  Recruitment,
} from '../../domain/recruitment/recruitment';
import { User } from '../../domain/user/user';
import { Settings } from './settings';

export const entities = [
  User,
  Application,
  ApplicationInterviewTime,
  ApplicationResultLog,
  ClubActivity,
  EmailAlarm,
  InterviewTime,
  Recruitment,
];

export function databaseOptions(settings: Settings): DataSourceOptions {
  return {
    type: 'mysql',
    host: settings.database.host,
    port: settings.database.port,
    database: settings.database.name,
    username: settings.database.username,
    password: settings.database.password,
    ssl: settings.database.ssl ? { rejectUnauthorized: true } : undefined,
    charset: 'utf8mb4',
    timezone: '+09:00',
    // Calendar dates must not pass through a host-timezone-dependent JS Date.
    dateStrings: ['DATE'],
    supportBigNumbers: true,
    bigNumberStrings: true,
    synchronize: false,
    migrationsRun: false,
    logging: false,
    entities,
    extra: { connectionLimit: 10 },
  };
}
