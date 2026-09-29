import { TemplateDto } from './board-requests';

export abstract class BoardTemplateRegister {
  abstract create(request: TemplateDto): Promise<void>;
  abstract update(id: string, request: TemplateDto): Promise<void>;
  abstract remove(id: string): Promise<void>;
}
