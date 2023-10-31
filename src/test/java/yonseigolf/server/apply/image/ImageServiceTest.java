package yonseigolf.server.apply.image;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@SpringBootTest
class ImageServiceTest {

    @MockBean
    private S3Client s3Client;
    @Autowired
    private ImageService imageService;

    @Test
    @DisplayName("사진을 업로드 하면 사진의 url을 반환 받을 수 있다.")
    void imageUploadTest() {
        // given
        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.jpg",
                        "image/jpeg",
                        "test image".getBytes());

        String expectedUrl = "https://yg-img-storage.s3.ap-northeast-2.amazonaws.com/store-image/test.jpgid";

        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).willReturn(null);

        // when
        String returnUrl = imageService.uploadImage(file, "id");

        // then
        assertThat(expectedUrl).isEqualTo(returnUrl);
    }

    @Test
    @DisplayName("파일에 문제가 있을 시 IllegalStateException을 반환한다.")
    void fileUploadThrowTest() throws IOException {
        // given
        MultipartFile file = mock(MultipartFile.class);
        given(file.getOriginalFilename()).willReturn("test.jpg");
        given(file.getContentType()).willReturn("image/jpeg");
        given(file.getSize()).willReturn(10L);
        given(file.getInputStream()).willThrow(IOException.class);  // This will throw IOException

        // when & then
        assertThatThrownBy(() -> imageService.uploadImage(file, "id"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to upload file");
    }
}
