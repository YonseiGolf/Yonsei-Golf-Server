import { Repository } from 'typeorm';
import { ApplicationResultLog } from '../../../domain/apply/application-result-log';

export abstract class ApplicationResultLogRepository extends Repository<ApplicationResultLog> {}
