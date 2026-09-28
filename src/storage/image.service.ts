import { randomUUID } from 'node:crypto';
import { PutObjectCommand, S3Client } from '@aws-sdk/client-s3';
import { getSignedUrl } from '@aws-sdk/s3-request-presigner';
import {
  BadRequestException,
  Injectable,
  OnModuleDestroy,
} from '@nestjs/common';
import { ImageUploadDto } from '../applications/applications.dto';
import { Settings } from '../config/settings';

@Injectable()
export class ImageService implements OnModuleDestroy {
  private readonly s3: S3Client;
  constructor(private readonly settings: Settings) {
    const storage = settings.storage;
    const minio = storage.provider === 'minio';
    this.s3 = new S3Client({
      region: storage.region,
      requestChecksumCalculation: 'WHEN_REQUIRED',
      endpoint: minio ? storage.endpoint : undefined,
      forcePathStyle: minio,
      credentials:
        minio && storage.accessKeyId && storage.secretAccessKey
          ? {
              accessKeyId: storage.accessKeyId,
              secretAccessKey: storage.secretAccessKey,
            }
          : undefined,
    });
  }
  async presign(dto: ImageUploadDto) {
    const extensions: Record<string, string> = {
      'image/jpeg': 'jpg',
      'image/png': 'png',
      'image/webp': 'webp',
      'image/gif': 'gif',
    };
    const extension = extensions[dto.contentType];
    if (!extension)
      throw new BadRequestException('지원하지 않는 이미지 형식입니다.');
    const imageKey = `store-image/${randomUUID()}.${extension}`;
    const publicRead = this.settings.storage.provider === 'minio';
    const command = new PutObjectCommand({
      Bucket: this.settings.storage.bucket,
      Key: imageKey,
      ContentType: dto.contentType,
      ContentLength: dto.fileSize,
      ...(publicRead ? { ACL: 'public-read' } : {}),
    });
    const uploadUrl = await getSignedUrl(this.s3, command, {
      expiresIn: 300,
      signableHeaders: new Set(['content-type', 'content-length']),
      unhoistableHeaders: new Set(['x-amz-acl']),
    });
    const uploadHeaders: Record<string, string> = {
      'Content-Type': dto.contentType,
    };
    if (publicRead) uploadHeaders['x-amz-acl'] = 'public-read';
    return { uploadUrl, imageKey, uploadHeaders };
  }
  resolve(key: string | null, legacyUrl: string | null): string | null {
    if (!key) return legacyUrl;
    const base = this.settings.storage.publicUrl.replace(/\/+$/, '');
    return this.settings.storage.provider === 'minio'
      ? `${base}/${this.settings.storage.bucket}/${key}`
      : `${base}/${key}`;
  }
  onModuleDestroy(): void {
    this.s3.destroy();
  }
}
