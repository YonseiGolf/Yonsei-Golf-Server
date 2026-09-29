import { IsEmail, MaxLength } from 'class-validator';

// Shared by the apply and recruitment slices, which must not import each other's requests.
export class EmailDto {
  @IsEmail() @MaxLength(255) email!: string;
}
