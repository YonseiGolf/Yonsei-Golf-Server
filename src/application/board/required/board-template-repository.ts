import { Repository } from 'typeorm';
import { BoardTemplate } from '../../../domain/board/board-template';

export abstract class BoardTemplateRepository extends Repository<BoardTemplate> {}
