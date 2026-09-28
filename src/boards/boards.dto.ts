import {
  IsEnum,
  IsNotEmpty,
  IsOptional,
  IsString,
  MaxLength,
} from 'class-validator';
import { PageQuery } from '../common/http';
import { Category } from './board.entity';

export class BoardDto {
  @IsEnum(Category) category!: Category;
  @IsString() @IsNotEmpty() @MaxLength(255) title!: string;
  @IsString() @IsNotEmpty() @MaxLength(16000) content!: string;
}
export class ReplyDto {
  @IsString() @IsNotEmpty() @MaxLength(16000) content!: string;
}
export class TemplateDto {
  @IsString() @IsNotEmpty() @MaxLength(255) title!: string;
  @IsString() @IsNotEmpty() @MaxLength(16000) contents!: string;
}
export class BoardsQuery extends PageQuery {
  @IsOptional() @IsEnum(Category) category?: Category;
}
