import {
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { formatDate } from '../common/dates';
import { apiId, pageResponse } from '../common/http';
import { Board, BoardTemplate, Reply } from './board.entity';
import { BoardDto, BoardsQuery, ReplyDto, TemplateDto } from './boards.dto';

@Injectable()
export class BoardsService {
  constructor(
    @InjectRepository(Board) private readonly boards: Repository<Board>,
    @InjectRepository(Reply) private readonly replies: Repository<Reply>,
    @InjectRepository(BoardTemplate)
    private readonly templates: Repository<BoardTemplate>,
  ) {}
  async list(query: BoardsQuery) {
    const builder = this.boards
      .createQueryBuilder('board')
      .innerJoinAndSelect('board.writer', 'writer')
      .where('board.deleted = :deleted', { deleted: 0 })
      .orderBy('board.id', 'DESC')
      .skip(query.page * query.size)
      .take(query.size);
    if (query.category)
      builder.andWhere('board.category = :category', {
        category: query.category,
      });
    const [boards, total] = await builder.getManyAndCount();
    return pageResponse(
      boards.map((board) => ({
        id: apiId(board.id),
        category: board.category,
        title: board.title,
        writer: board.writer?.name,
        createdAt: formatDate(board.createdAt, 'short'),
      })),
      total,
      query,
    );
  }
  async create(dto: BoardDto, userId: string): Promise<void> {
    await this.boards.save(
      this.boards.create({
        ...dto,
        userId,
        createdAt: new Date(),
        deleted: false,
      }),
    );
  }
  private async findBoard(id: string): Promise<Board> {
    const board = await this.boards.findOne({
      where: { id },
      relations: { writer: true },
    });
    if (!board || board.deleted || !board.writer)
      throw new NotFoundException('해당 게시글이 존재하지 않습니다.');
    return board;
  }
  async detail(id: string) {
    const board = await this.findBoard(id);
    const replies = await this.replies.find({
      where: { boardId: id },
      relations: { writer: true },
      order: { id: 'ASC' },
    });
    return {
      id: apiId(board.id),
      writerId: apiId(board.userId),
      writer: board.writer?.name,
      category: board.category,
      title: board.title,
      content: board.content,
      createdAt: formatDate(board.createdAt, 'full'),
      replies: {
        replies: replies.map((reply) => ({
          id: apiId(reply.id),
          writerId: apiId(reply.userId),
          writer: reply.writer?.name ?? '',
          content: reply.content,
          createdAt: formatDate(reply.createdAt, 'full'),
        })),
      },
    };
  }
  async update(id: string, dto: BoardDto, userId: string): Promise<void> {
    const board = await this.findBoard(id);
    this.checkOwner(board.userId, userId);
    await this.boards.update(id, dto);
  }
  async remove(id: string, userId: string): Promise<void> {
    const board = await this.findBoard(id);
    this.checkOwner(board.userId, userId);
    await this.boards.update(id, { deleted: true });
  }
  async reply(boardId: string, dto: ReplyDto, userId: string): Promise<void> {
    await this.findBoard(boardId);
    await this.replies.save(
      this.replies.create({ ...dto, boardId, userId, createdAt: new Date() }),
    );
  }
  async removeReply(id: string, userId: string): Promise<void> {
    const reply = await this.replies.findOneBy({ id });
    if (!reply) throw new NotFoundException('해당 댓글이 존재하지 않습니다.');
    this.checkOwner(reply.userId, userId);
    await this.replies.delete(id);
  }
  private checkOwner(owner: string, userId: string): void {
    if (String(owner) !== userId)
      throw new ForbiddenException('작성자만 수정/삭제할 수 있습니다.');
  }
  async listTemplates() {
    return {
      templates: (await this.templates.find({ order: { id: 'ASC' } })).map(
        (template) => ({ id: apiId(template.id), title: template.title }),
      ),
    };
  }
  async template(id: string) {
    const template = await this.templates.findOneBy({ id });
    if (!template)
      throw new NotFoundException('해당 게시글 템플릿이 존재하지 않습니다.');
    return { ...template, id: apiId(template.id) };
  }
  async createTemplate(dto: TemplateDto): Promise<void> {
    await this.templates.save(this.templates.create(dto));
  }
  async updateTemplate(id: string, dto: TemplateDto): Promise<void> {
    await this.template(id);
    await this.templates.update(id, dto);
  }
  async removeTemplate(id: string): Promise<void> {
    await this.template(id);
    await this.templates.delete(id);
  }
}
