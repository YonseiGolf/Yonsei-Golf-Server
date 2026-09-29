import { BadRequestException, PipeTransform } from '@nestjs/common';

/** Path IDs stay strings so a MySQL BIGINT is never rounded. */
export class IdPipe implements PipeTransform<string, string> {
  transform(value: string): string {
    if (!/^[1-9]\d*$/.test(value) || BigInt(value) > 9223372036854775807n)
      throw new BadRequestException('올바른 ID를 입력해주세요.');
    return value;
  }
}
