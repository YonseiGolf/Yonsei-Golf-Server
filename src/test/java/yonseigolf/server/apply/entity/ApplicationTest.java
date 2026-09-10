package yonseigolf.server.apply.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yonseigolf.server.apply.dto.request.ApplicationRequest;

class ApplicationTest {

    @Test
    @DisplayName("신규 지원서는 전체 이미지 URL 대신 이미지 key를 저장한다.")
    void createApplicationWithPhotoKey() {
        // given
        ApplicationRequest request = ApplicationRequest.builder()
                .photoKey("store-image/photo.png")
                .build();

        // when
        Application application = Application.of(request);

        // then
        assertThat(application.getPhotoKey()).isEqualTo("store-image/photo.png");
        assertThat(application.getPhoto()).isNull();
    }
}
