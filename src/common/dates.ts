import { BadRequestException } from '@nestjs/common';

export function localDateTime(value: string): Date {
  if (!/^\d{4}-\d{2}-\d{2}[T ]\d{2}:\d{2}(:\d{2}(\.\d{1,3})?)?$/.test(value))
    throw new BadRequestException('한국 시간의 날짜와 시간을 입력해주세요.');
  const date = new Date(`${value.replace(' ', 'T')}+09:00`);
  if (Number.isNaN(date.getTime()))
    throw new BadRequestException('올바른 날짜와 시간을 입력해주세요.');
  return date;
}

export function seoulIso(date = new Date()): string {
  return new Date(date.getTime() + 9 * 60 * 60 * 1000)
    .toISOString()
    .slice(0, 19);
}

export function formatDate(
  value: Date | string | null,
  pattern: 'short' | 'full' | 'month' | 'monthTime' | 'interview',
): string | null {
  if (value === null) return null;
  const iso = value instanceof Date ? seoulIso(value) : value;
  const year = iso.slice(0, 4),
    month = iso.slice(5, 7),
    day = iso.slice(8, 10),
    time = iso.slice(11, 16);
  switch (pattern) {
    case 'short':
      return `${year.slice(2)}-${month}-${day}`;
    case 'full':
      return `${year}년 ${month}월 ${day}일 ${iso.slice(11, 19)}`;
    case 'month':
      return `${month}월${day}일`;
    case 'monthTime':
      return `${month}월${day}일 ${time}`;
    case 'interview':
      return `${year}-${month}-${day} ${time}`;
  }
}
