import { Repository } from 'typeorm';
import { InterviewTime } from '../../../domain/recruitment/recruitment';

export abstract class InterviewTimeRepository extends Repository<InterviewTime> {}
