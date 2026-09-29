import { Repository } from 'typeorm';
import { Application } from '../../../domain/apply/application';

export abstract class ApplicationRepository extends Repository<Application> {}
