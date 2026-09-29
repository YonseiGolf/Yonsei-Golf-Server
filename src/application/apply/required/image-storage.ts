export interface PresignedUpload {
  uploadUrl: string;
  /** Headers the browser must send with the PUT for the signature to match. */
  uploadHeaders: Record<string, string>;
}

export abstract class ImageStorage {
  abstract presignUpload(object: {
    key: string;
    contentType: string;
    contentLength: number;
  }): Promise<PresignedUpload>;
  abstract publicUrl(key: string): string;
}
