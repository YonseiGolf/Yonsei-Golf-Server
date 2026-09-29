import { Repository } from 'typeorm';
import { ApplicationInterviewTime } from '../../../domain/apply/application';

export abstract class ApplicationInterviewTimeRepository extends Repository<ApplicationInterviewTime> {}
