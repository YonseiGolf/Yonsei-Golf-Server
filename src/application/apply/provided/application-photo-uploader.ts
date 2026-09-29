import { ImageUploadDto } from './apply-requests';
import { PhotoUploadResponse } from './apply-responses';

/** The browser uploads the photo straight to storage with the issued URL. */
export abstract class ApplicationPhotoUploader {
  abstract issueUploadUrl(
    request: ImageUploadDto,
  ): Promise<PhotoUploadResponse>;
}
