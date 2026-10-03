import { Repository } from 'typeorm';
import { MailTemplate } from '../../../domain/mail/mail-template';

export abstract class MailTemplateRepository extends Repository<MailTemplate> {}
