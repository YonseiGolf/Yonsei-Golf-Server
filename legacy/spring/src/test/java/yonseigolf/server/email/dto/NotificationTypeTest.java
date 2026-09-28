package yonseigolf.server.email.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class NotificationTypeTest {

    @Test
    void testDocumentPassMessage() {
        // given
        String name = "John";
        String expectedMessage = "John님 서류 합격 축하드립니다. \n면접 일정은 추후 공지될 예정입니다. \n감사합니다.";

        // when
        String result = NotificationType.DOCUMENT_PASS.generateMessage(name);

        assertThat(result).isEqualTo(expectedMessage);
    }

    @Test
    void testFinalPassMessage() {
        // given
        String name = "John";
        String expectedMessage = "John님 최종 합격 축하드립니다. \n추후 일정은 문자로 공지될 예정입니다. \n감사합니다.";

        // when
        String result = NotificationType.FINAL_PASS.generateMessage(name);

        assertThat(result).isEqualTo(expectedMessage);
    }

    @Test
    void testFailMessage() {
        // given
        String name = "John";
        String expectedMessage = "John님 연세골프에 지원해주셔서 감사합니다. \n\n\n" +
                "안타깝게도 John님께 이번 연골 모집에서 합격의 소식을 전해드리지 못하게 되었습니다." +
                "John님의 뛰어난 열정에도 불구하고, 연세골프는 한정된 인원으로만 운영되는 만큼 아쉽게도 이런 소식을 전해드리게 됐습니다." +
                "비록 이번 모집에서 John님과 함께하지 못하지만, 다음에 함께 할 수 있기를 바라겠습니다. \n\n" +
                "바쁘신 와중에 지원해주셔서 감사합니다. \n\n" +
                "연세 골프 운영진 드림";

        // when
        String result = NotificationType.FAIL.generateMessage(name);

        assertThat(result).isEqualTo(expectedMessage);
    }
}
