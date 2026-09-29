import { Repository } from 'typeorm';
import { Recruitment } from '../../../domain/recruitment/recruitment';

export abstract class RecruitmentRepository extends Repository<Recruitment> {}
