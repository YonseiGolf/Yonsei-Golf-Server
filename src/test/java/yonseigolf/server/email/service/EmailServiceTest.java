package yonseigolf.server.email.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import yonseigolf.server.apply.entity.EmailAlarm;
import yonseigolf.server.apply.repository.EmailRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailRepository emailRepository;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("지원 시작 이메일을 보낸다.")
    public void sendApplyStartAlertTest() {
        // Given
        List<EmailAlarm> alerts = Arrays.asList(
                EmailAlarm.builder()
                        .id(1L)
                        .email("email")
                        .build()
        );
        given(emailRepository.findAll()).willReturn(alerts);

        // When
        emailService.sendApplyStartAlert();

        // Then
        assertAll(
                () -> verify(emailRepository, times(1)).findAll(),
                () -> verify(mailSender, times(alerts.size())).send(any(SimpleMailMessage.class)),
                () -> verify(emailRepository, times(1)).deleteAll()
                );
    }

    @Test
    @DisplayName("이메일 전송 시 예외가 발생했을 경우, catch 후 illeagalArgumentException을 발생시킨다.")
    void testSendEmail_ExceptionThrown() {
        // given
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "Test Text";

        doThrow(new IllegalArgumentException()).when(mailSender).send(any(SimpleMailMessage.class));

        // then
        assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendEmail(to, subject, text);
        });
    }
}
