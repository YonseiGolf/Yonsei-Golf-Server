package yonseigolf.server.apply.image;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import yonseigolf.server.apply.dto.request.ImageUploadRequest;
import yonseigolf.server.apply.dto.response.ImageResponse;

import java.time.Duration;
import java.util.Map;

@Service
public class ImageService {

    private static final Duration UPLOAD_URL_EXPIRATION = Duration.ofMinutes(5);
    private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif"
    );

    private final S3Presigner s3Presigner;
    private final ImageStoragePolicy storagePolicy;
    @Value("${AWS_S3_BUCKET}")
    private String bucketName;
    @Value("${AWS_S3_PUBLIC_URL:https://minio.up-api.kr}")
    private String publicUrl;

    public ImageService(S3Presigner s3Presigner, ImageStoragePolicy storagePolicy) {

        this.s3Presigner = s3Presigner;
        this.storagePolicy = storagePolicy;
    }

    public ImageResponse createPresignedUpload(ImageUploadRequest request, String randomId) {

        String extension = IMAGE_EXTENSIONS.get(request.getContentType());
        if (extension == null) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다.");
        }

        String objectKey = "store-image/" + randomId + extension;
        PutObjectRequest.Builder putObjectRequestBuilder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(request.getContentType())
                .contentLength(request.getFileSize());

        if (storagePolicy.publicReadAcl()) {
            putObjectRequestBuilder.acl(ObjectCannedACL.PUBLIC_READ);
        }

        PutObjectRequest putObjectRequest = putObjectRequestBuilder.build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(UPLOAD_URL_EXPIRATION)
                .putObjectRequest(putObjectRequest)
                .build();
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        Map<String, String> uploadHeaders = storagePolicy.publicReadAcl()
                ? Map.of(
                        "Content-Type", request.getContentType(),
                        "x-amz-acl", "public-read")
                : Map.of("Content-Type", request.getContentType());

        return new ImageResponse(
                presignedRequest.url().toString(),
                objectKey,
                uploadHeaders);
    }

    public String resolveImageUrl(String imageKey, String legacyImageUrl) {

        if (imageKey == null || imageKey.isBlank()) {
            return legacyImageUrl;
        }

        String baseUrl = publicUrl.replaceAll("/$", "");
        if (storagePolicy.includeBucketInPublicUrl()) {
            return String.format("%s/%s/%s", baseUrl, bucketName, imageKey);
        }

        return String.format("%s/%s", baseUrl, imageKey);
    }
}
