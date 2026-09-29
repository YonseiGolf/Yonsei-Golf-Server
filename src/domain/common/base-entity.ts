import { PrimaryGeneratedColumn, ValueTransformer } from 'typeorm';

export abstract class BaseEntity {
  @PrimaryGeneratedColumn({ type: 'bigint' })
  id!: string;
}

export const bitBoolean: ValueTransformer = {
  to: (value: boolean | null) => (value === null ? null : value ? 1 : 0),
  from: (value: Buffer | number | boolean | null) =>
    value === null
      ? null
      : Buffer.isBuffer(value)
        ? value[0] === 1
        : Boolean(value),
};
