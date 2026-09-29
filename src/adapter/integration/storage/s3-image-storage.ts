import { PutObjectCommand, S3Client } from '@aws-sdk/client-s3';
import { getSignedUrl } from '@aws-sdk/s3-request-presigner';
import { OnModuleDestroy } from '@nestjs/common';
import {
  ImageStorage,
  PresignedUpload,
} from '../../../application/apply/required/image-storage';
import { Adapter } from '../../../support/stereotype';
import { Settings } from '../../config/settings';

/**
 * S3 or MinIO. MinIO objects are public-read and served from the bucket path;
 * S3 objects stay private behind CloudFront (`publicUrl`).
 */
@Adapter()
export class S3ImageStorage implements ImageStorage, OnModuleDestroy {
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
  async presignUpload(object: {
    key: string;
    contentType: string;
    contentLength: number;
  }): Promise<PresignedUpload> {
    const publicRead = this.settings.storage.provider === 'minio';
    const command = new PutObjectCommand({
      Bucket: this.settings.storage.bucket,
      Key: object.key,
      ContentType: object.contentType,
      ContentLength: object.contentLength,
      ...(publicRead ? { ACL: 'public-read' } : {}),
    });
    const uploadUrl = await getSignedUrl(this.s3, command, {
      expiresIn: 300,
      signableHeaders: new Set(['content-type', 'content-length']),
      unhoistableHeaders: new Set(['x-amz-acl']),
    });
    const uploadHeaders: Record<string, string> = {
      'Content-Type': object.contentType,
    };
    if (publicRead) uploadHeaders['x-amz-acl'] = 'public-read';
    return { uploadUrl, uploadHeaders };
  }
  publicUrl(key: string): string {
    const base = this.settings.storage.publicUrl.replace(/\/+$/, '');
    return this.settings.storage.provider === 'minio'
      ? `${base}/${this.settings.storage.bucket}/${key}`
      : `${base}/${key}`;
  }
  onModuleDestroy(): void {
    this.s3.destroy();
  }
}
