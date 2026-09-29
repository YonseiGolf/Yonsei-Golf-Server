import { BoardTemplate } from '../../domain/board/board-template';
import { NotFoundError } from '../../support/errors';
import { ApplicationService } from '../../support/stereotype';
import { apiId } from '../shared/api-id';
import { TemplateDto } from './provided/board-requests';
import {
  TemplateResponse,
  TemplateSummaryResponse,
} from './provided/board-responses';
import { BoardTemplateFinder } from './provided/board-template-finder';
import { BoardTemplateRegister } from './provided/board-template-register';
import { BoardTemplateRepository } from './required/board-template-repository';

@ApplicationService()
export class BoardTemplateService
  implements BoardTemplateRegister, BoardTemplateFinder
{
  constructor(private readonly templates: BoardTemplateRepository) {}

  async list(): Promise<{ templates: TemplateSummaryResponse[] }> {
    return {
      templates: (await this.templates.find({ order: { id: 'ASC' } })).map(
        (template) => ({ id: apiId(template.id), title: template.title }),
      ),
    };
  }
  async find(id: string): Promise<TemplateResponse> {
    const template = await this.templates.findOneBy({ id });
    if (!template)
      throw new NotFoundError('해당 게시글 템플릿이 존재하지 않습니다.');
    return { ...template, id: apiId(template.id) };
  }
  async create(request: TemplateDto): Promise<void> {
    await this.templates.save(BoardTemplate.create(request));
  }
  async update(id: string, request: TemplateDto): Promise<void> {
    await this.find(id);
    await this.templates.update(id, {
      title: request.title,
      contents: request.contents,
    });
  }
  async remove(id: string): Promise<void> {
    await this.find(id);
    await this.templates.delete(id);
  }
}
