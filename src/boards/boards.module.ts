import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Board, BoardTemplate, Reply } from './board.entity';
import { BoardsController } from './boards.controller';
import { BoardsService } from './boards.service';

@Module({
  imports: [TypeOrmModule.forFeature([Board, Reply, BoardTemplate])],
  controllers: [BoardsController],
  providers: [BoardsService],
})
export class BoardsModule {}
