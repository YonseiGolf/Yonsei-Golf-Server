import { Repository } from 'typeorm';
import { ClubActivity } from '../../../domain/apply/application';

export abstract class ClubActivityRepository extends Repository<ClubActivity> {}
