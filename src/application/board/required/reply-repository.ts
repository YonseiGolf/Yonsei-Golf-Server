import { Repository } from 'typeorm';
import { Reply } from '../../../domain/board/board';

export abstract class ReplyRepository extends Repository<Reply> {}
