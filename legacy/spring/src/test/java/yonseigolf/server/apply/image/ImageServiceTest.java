package yonseigolf.server.apply.image;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import yonseigolf.server.apply.dto.request.ImageUploadRequest;
import yonseigolf.server.apply.dto.response.ImageResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private S3Presigner s3Presigner;
    private ImageService imageService;

    @BeforeEach
    void setUp() {
        imageService = new ImageService(s3Presigner, new ImageStoragePolicy(true, true));
        ReflectionTestUtils.setField(imageService, "bucketName", "yg-img-storage");
        ReflectionTestUtils.setField(imageService, "publicUrl", "https://minio.up-api.kr");
    }

    @Test
    @DisplayName("사진 업로드용 presigned URL과 최종 이미지 URL을 발급한다.")
    void createPresignedUploadTest() {
        // given
        ImageUploadRequest request = new ImageUploadRequest("profile.png", "image/png", 1024L);
        PresignedPutObjectRequest presignedRequest = PresignedPutObjectRequest.builder()
                .expiration(Instant.now().plusSeconds(300))
                .isBrowserExecutable(true)
                .signedHeaders(Collections.singletonMap(
                        "host", Collections.singletonList("minio.up-api.kr")))
                .httpRequest(SdkHttpFullRequest.builder()
                        .method(SdkHttpMethod.PUT)
                        .uri(URI.create("https://minio.up-api.kr/signed-upload"))
                        .build())
                .build();
        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .willReturn(presignedRequest);

        // when
        ImageResponse response = imageService.createPresignedUpload(request, "random-id");

        // then
        assertThat(response.getUploadUrl()).isEqualTo("https://minio.up-api.kr/signed-upload");
        assertThat(response.getImageKey()).isEqualTo("store-image/random-id.png");
        assertThat(response.getUploadHeaders())
                .containsEntry("Content-Type", "image/png")
                .containsEntry("x-amz-acl", "public-read");

        ArgumentCaptor<PutObjectPresignRequest> captor =
                ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(captor.capture());
        assertThat(captor.getValue().putObjectRequest().bucket()).isEqualTo("yg-img-storage");
        assertThat(captor.getValue().putObjectRequest().key()).isEqualTo("store-image/random-id.png");
        assertThat(captor.getValue().putObjectRequest().contentType()).isEqualTo("image/png");
        assertThat(captor.getValue().putObjectRequest().contentLength()).isEqualTo(1024L);
        assertThat(captor.getValue().putObjectRequest().aclAsString()).isEqualTo("public-read");
    }

    @Test
    @DisplayName("이미지 key를 공개 이미지 URL로 변환한다.")
    void resolveImageUrlTest() {
        // when
        String imageUrl = imageService.resolveImageUrl("store-image/random-id.png", null);

        // then
        assertThat(imageUrl)
                .isEqualTo("https://minio.up-api.kr/yg-img-storage/store-image/random-id.png");
    }

    @Test
    @DisplayName("이미지 key가 없으면 기존 이미지 URL을 반환한다.")
    void resolveLegacyImageUrlTest() {
        // when
        String imageUrl = imageService.resolveImageUrl(null, "https://legacy/image.png");

        // then
        assertThat(imageUrl).isEqualTo("https://legacy/image.png");
    }

    @Test
    @DisplayName("지원하지 않는 이미지 형식은 presigned URL을 발급하지 않는다.")
    void unsupportedImageTypeTest() {
        // given
        ImageUploadRequest request = new ImageUploadRequest("profile.svg", "image/svg+xml", 1024L);

        // when & then
        assertThatThrownBy(() -> imageService.createPresignedUpload(request, "random-id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지원하지 않는 이미지 형식입니다.");
    }

    @Test
    @DisplayName("presigned URL은 브라우저가 전송할 업로드 헤더를 서명한다.")
    void presignedUploadHeadersTest() {
        try (S3Presigner presigner = S3Presigner.builder()
                .endpointOverride(URI.create("https://minio.up-api.kr"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test-access-key", "test-secret-key")))
                .region(Region.AP_NORTHEAST_2)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build()) {
            ImageService service = new ImageService(
                    presigner,
                    new ImageStoragePolicy(true, true));
            ReflectionTestUtils.setField(service, "bucketName", "yg-img-storage");
            ReflectionTestUtils.setField(service, "publicUrl", "https://minio.up-api.kr");

            ImageResponse response = service.createPresignedUpload(
                    new ImageUploadRequest("profile.jpg", "image/jpeg", 1024L),
                    "random-id");
            String decodedQuery = URLDecoder.decode(
                    URI.create(response.getUploadUrl()).getRawQuery(), StandardCharsets.UTF_8);

            assertThat(decodedQuery).contains(
                    "X-Amz-SignedHeaders=content-length;content-type;host;x-amz-acl");
        }
    }

    @Test
    @DisplayName("AWS 프로필은 private S3 객체를 만들고 CloudFront URL을 반환한다.")
    void awsStoragePolicyTest() {
        // given
        ImageService service = new ImageService(
                s3Presigner,
                new ImageStoragePolicy(false, false));
        ReflectionTestUtils.setField(service, "bucketName", "yg-server-dev-assets");
        ReflectionTestUtils.setField(service, "publicUrl", "https://image-tmp.up-api.kr/");
        PresignedPutObjectRequest presignedRequest = PresignedPutObjectRequest.builder()
                .expiration(Instant.now().plusSeconds(300))
                .isBrowserExecutable(true)
                .signedHeaders(Collections.singletonMap(
                        "host", Collections.singletonList("s3.ap-northeast-2.amazonaws.com")))
                .httpRequest(SdkHttpFullRequest.builder()
                        .method(SdkHttpMethod.PUT)
                        .uri(URI.create("https://s3.ap-northeast-2.amazonaws.com/signed-upload"))
                        .build())
                .build();
        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .willReturn(presignedRequest);

        // when
        ImageResponse response = service.createPresignedUpload(
                new ImageUploadRequest("profile.webp", "image/webp", 2048L),
                "aws-random-id");

        // then
        ArgumentCaptor<PutObjectPresignRequest> captor =
                ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(captor.capture());
        assertThat(captor.getValue().putObjectRequest().acl()).isNull();
        assertThat(response.getImageKey()).isEqualTo("store-image/aws-random-id.webp");
        assertThat(response.getUploadHeaders())
                .containsExactly(Map.entry("Content-Type", "image/webp"));
        assertThat(service.resolveImageUrl(response.getImageKey(), null))
                .isEqualTo("https://image-tmp.up-api.kr/store-image/aws-random-id.webp");
    }
}
