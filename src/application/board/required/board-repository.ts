import { Repository } from 'typeorm';
import { Board } from '../../../domain/board/board';

export abstract class BoardRepository extends Repository<Board> {}
