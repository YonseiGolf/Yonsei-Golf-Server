import { randomUUID } from 'node:crypto';
import { InvalidInputError } from '../../support/errors';
import { ApplicationService } from '../../support/stereotype';
import { ApplicationPhotoUploader } from './provided/application-photo-uploader';
import { ImageUploadDto } from './provided/apply-requests';
import { PhotoUploadResponse } from './provided/apply-responses';
import { ImageStorage } from './required/image-storage';

// ApplicationDto.photoKey only accepts keys of this shape.
const EXTENSIONS: Record<string, string> = {
  'image/jpeg': 'jpg',
  'image/png': 'png',
  'image/webp': 'webp',
  'image/gif': 'gif',
};

@ApplicationService()
export class ApplicationPhotoService implements ApplicationPhotoUploader {
  constructor(private readonly storage: ImageStorage) {}

  async issueUploadUrl(request: ImageUploadDto): Promise<PhotoUploadResponse> {
    const extension = EXTENSIONS[request.contentType];
    if (!extension)
      throw new InvalidInputError('지원하지 않는 이미지 형식입니다.');
    const imageKey = `store-image/${randomUUID()}.${extension}`;
    const { uploadUrl, uploadHeaders } = await this.storage.presignUpload({
      key: imageKey,
      contentType: request.contentType,
      contentLength: request.fileSize,
    });
    return { uploadUrl, imageKey, uploadHeaders };
  }
  /** Spring-era rows only have a full URL in `photo`. */
  photoUrl(key: string | null, legacyUrl: string | null): string | null {
    return key ? this.storage.publicUrl(key) : legacyUrl;
  }
}
