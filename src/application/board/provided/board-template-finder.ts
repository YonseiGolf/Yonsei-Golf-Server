import { TemplateResponse, TemplateSummaryResponse } from './board-responses';

export abstract class BoardTemplateFinder {
  abstract list(): Promise<{ templates: TemplateSummaryResponse[] }>;
  abstract find(id: string): Promise<TemplateResponse>;
}
