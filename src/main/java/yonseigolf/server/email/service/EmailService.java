package yonseigolf.server.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import yonseigolf.server.apply.entity.EmailAlarm;
import yonseigolf.server.apply.repository.EmailRepository;
import yonseigolf.server.email.dto.NotificationType;
import yonseigolf.server.email.dto.response.AllWaitingEmail;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String APPLICATION_EMAIL_CONFIRMATION_SUBJECT =
            "[연세골프] 지원서 이메일 확인";
    private static final String APPLICATION_EMAIL_CONFIRMATION_MESSAGE = String.join("\n",
            "안녕하세요 연세대학교 골프동아리입니다.",
            "",
            "지원서에 작성해주신 이메일 주소 확인을 위해 발송한 메일입니다.",
            "이 메일을 정상적으로 받으셨다면 입력하신 이메일 주소로 지원 결과가 안내됩니다.",
            "",
            "감사합니다.",
            "연세대학교 골프동아리 드림");

    private final JavaMailSender mailSender;
    private final EmailRepository emailRepository;

    public AllWaitingEmail findAllWaitingEmail(int semester) {

        return AllWaitingEmail.builder()
                .emailAlarms(findAllAlert(semester))
                .build();
    }

    public void sendApplyStartAlert() {
        List<EmailAlarm> unsentAlarm = emailRepository.findAllBySentAtIsNull();

        if (unsentAlarm.isEmpty()) {
            log.info("발송할 이메일이 없습니다.");
            return;
        }

        String[] bccAddresses = unsentAlarm.stream()
                .map(EmailAlarm::getEmail)
                .toArray(String[]::new);

        sendEmail(bccAddresses,
                "연세대학교 골프동아리입니다.",
                NotificationType.CLUB_RECRUITMENT.generateMessage(null));

        unsentAlarm.forEach(EmailAlarm::markAsSent);
        emailRepository.saveAll(unsentAlarm);
    }

    private List<EmailAlarm> findAllAlert(int semester) {

        return emailRepository.findAllBySemester(semester);
    }

    private void sendEmail(String[] bcc, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setBcc(bcc);
        message.setSubject(subject);
        message.setText(text);
        sendEmailMessage(message);
    }

    public void sendEmail(String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        sendEmailMessage(message);
    }

    public void sendApplicationEmailConfirmation(String email) {
        sendEmail(email, APPLICATION_EMAIL_CONFIRMATION_SUBJECT,
                APPLICATION_EMAIL_CONFIRMATION_MESSAGE);
    }

    private void sendEmailMessage(SimpleMailMessage message) {
        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new IllegalArgumentException("이메일 전송에 실패했습니다.", e);
        }
    }
}
