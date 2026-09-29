import { Column, Entity } from 'typeorm';
import { BaseEntity } from '../common/base-entity';

export interface TemplateContent {
  title: string;
  contents: string;
}

@Entity('board_template')
export class BoardTemplate extends BaseEntity {
  @Column({ type: 'varchar', length: 255 }) title!: string;
  @Column({ type: 'text' }) contents!: string;

  static create(content: TemplateContent): BoardTemplate {
    const template = new BoardTemplate();
    template.title = content.title;
    template.contents = content.contents;
    return template;
  }
}
