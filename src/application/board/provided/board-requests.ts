import {
  IsEnum,
  IsNotEmpty,
  IsOptional,
  IsString,
  MaxLength,
} from 'class-validator';
import { BoardPost, Category } from '../../../domain/board/board';
import { TemplateContent } from '../../../domain/board/board-template';
import { PageQuery } from '../../shared/page';

export class BoardDto implements BoardPost {
  @IsEnum(Category) category!: Category;
  @IsString() @IsNotEmpty() @MaxLength(255) title!: string;
  @IsString() @IsNotEmpty() @MaxLength(16000) content!: string;
}
export class ReplyDto {
  @IsString() @IsNotEmpty() @MaxLength(16000) content!: string;
}
export class TemplateDto implements TemplateContent {
  @IsString() @IsNotEmpty() @MaxLength(255) title!: string;
  @IsString() @IsNotEmpty() @MaxLength(16000) contents!: string;
}
export class BoardsQuery extends PageQuery {
  @IsOptional() @IsEnum(Category) category?: Category;
}
