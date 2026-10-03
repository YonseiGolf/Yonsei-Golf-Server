import { IsEnum, IsString, MaxLength } from 'class-validator';
import { MailTemplateType } from '../../../domain/mail/mail-template';

export class MailTemplateTypeParam {
  @IsEnum(MailTemplateType) type!: MailTemplateType;
}
export class MailTemplateDto {
  @IsString() @MaxLength(255) subject!: string;
  @IsString() @MaxLength(10000) body!: string;
}
