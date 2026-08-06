package yonseigolf.server.email.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import yonseigolf.server.apply.repository.EmailRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceUnitTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private EmailRepository emailRepository;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender, emailRepository);
    }

    @Test
    @DisplayName("지원서 이메일 확인 메일을 발송한다.")
    void sendApplicationEmailConfirmationTest() {
        // given
        String email = "applicant@example.com";
        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        // when
        emailService.sendApplicationEmailConfirmation(email);

        // then
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();
        assertThat(message.getTo()).containsExactly(email);
        assertThat(message.getSubject()).isEqualTo("[연세골프] 지원서 이메일 확인");
        assertThat(message.getText())
                .startsWith("안녕하세요 연세대학교 골프동아리입니다.")
                .contains("지원서에 작성해주신 이메일 주소 확인을 위해 발송한 메일입니다.");
    }
}
