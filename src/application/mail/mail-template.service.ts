import {
  MailTemplate,
  MailTemplateType,
  placeholdersOf,
} from '../../domain/mail/mail-template';
import { ApplicationService } from '../../support/stereotype';
import { defaultMailTemplates } from './default-mail-templates';
import { MailTemplateFinder } from './provided/mail-template-finder';
import { MailTemplateRegister } from './provided/mail-template-register';
import { MailTemplateDto } from './provided/mail-template-requests';
import { MailTemplateResponse } from './provided/mail-template-responses';
import { MailTemplateRepository } from './required/mail-template-repository';

@ApplicationService()
export class MailTemplateService
  implements MailTemplateFinder, MailTemplateRegister
{
  constructor(private readonly templates: MailTemplateRepository) {}

  async list(): Promise<MailTemplateResponse[]> {
    const saved = await this.templates.find();
    return Object.values(MailTemplateType).map((type) => {
      const template = saved.find((item) => item.type === type);
      const { subject, body } = template ?? defaultMailTemplates[type];
      return {
        type,
        subject,
        body,
        placeholders: placeholdersOf(type),
        customized: template !== undefined,
      };
    });
  }
  async find(type: MailTemplateType): Promise<MailTemplate> {
    const { subject, body } = defaultMailTemplates[type];
    return (
      (await this.templates.findOneBy({ type })) ??
      MailTemplate.write(type, subject, body)
    );
  }
  // Two admins saving a never-saved type at once hit the unique key: one gets 409.
  async update(
    type: MailTemplateType,
    request: MailTemplateDto,
  ): Promise<void> {
    const template = await this.templates.findOneBy({ type });
    if (template) template.rewrite(request.subject, request.body);
    await this.templates.save(
      template ?? MailTemplate.write(type, request.subject, request.body),
    );
  }
  async reset(type: MailTemplateType): Promise<void> {
    await this.templates.delete({ type });
  }
}
