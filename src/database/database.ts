import { DataSourceOptions } from 'typeorm';
import {
  Application,
  ApplicationInterviewTime,
  ApplicationResultLog,
  ClubActivity,
  EmailAlarm,
  InterviewTime,
  Recruitment,
} from '../applications/application.entity';
import { Board, BoardTemplate, Reply } from '../boards/board.entity';
import { Settings } from '../config/settings';
import { User } from '../users/user.entity';

export const entities = [
  User,
  Board,
  Reply,
  BoardTemplate,
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
    supportBigNumbers: true,
    bigNumberStrings: true,
    synchronize: false,
    migrationsRun: false,
    logging: false,
    entities,
    extra: { connectionLimit: 10 },
  };
}
