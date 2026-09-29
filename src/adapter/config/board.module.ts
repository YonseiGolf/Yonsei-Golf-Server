import { Module } from '@nestjs/common';
import { BoardService } from '../../application/board/board.service';
import { BoardQueryService } from '../../application/board/board-query.service';
import { BoardTemplateService } from '../../application/board/board-template.service';
import { BoardFinder } from '../../application/board/provided/board-finder';
import { BoardRegister } from '../../application/board/provided/board-register';
import { BoardTemplateFinder } from '../../application/board/provided/board-template-finder';
import { BoardTemplateRegister } from '../../application/board/provided/board-template-register';
import { BoardRepository } from '../../application/board/required/board-repository';
import { BoardTemplateRepository } from '../../application/board/required/board-template-repository';
import { ReplyRepository } from '../../application/board/required/reply-repository';
import { Board, Reply } from '../../domain/board/board';
import { BoardTemplate } from '../../domain/board/board-template';
import { BoardController } from '../webapi/board/board.controller';
import { BoardTemplateController } from '../webapi/board/board-template.controller';
import { repositoryProvider } from './repository';

@Module({
  controllers: [BoardController, BoardTemplateController],
  providers: [
    repositoryProvider(BoardRepository, Board),
    repositoryProvider(ReplyRepository, Reply),
    repositoryProvider(BoardTemplateRepository, BoardTemplate),
    BoardQueryService,
    BoardService,
    BoardTemplateService,
    { provide: BoardFinder, useExisting: BoardQueryService },
    { provide: BoardRegister, useExisting: BoardService },
    { provide: BoardTemplateFinder, useExisting: BoardTemplateService },
    { provide: BoardTemplateRegister, useExisting: BoardTemplateService },
  ],
})
export class BoardModule {}
