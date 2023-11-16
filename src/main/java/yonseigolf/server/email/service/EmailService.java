package yonseigolf.server.email.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailRepository emailRepository;

    @Autowired
    public EmailService(JavaMailSender mailSender, EmailRepository emailRepository) {

        this.mailSender = mailSender;
        this.emailRepository = emailRepository;
    }

    public AllWaitingEmail findAllWaitingEmail() {

        return AllWaitingEmail.builder()
                .emailAlarms(findAllAlert())
                .build();
    }

    public void sendApplyStartAlert() {
        List<EmailAlarm> allAlert = findAllAlert();

        allAlert.forEach(alert -> sendEmail(alert.getEmail(),
                "연세대학교 골프동아리입니다.",
                NotificationType.CLUB_RECRUITMENT.generateMessage(null)));

        emailRepository.deleteAll();
    }


    private List<EmailAlarm> findAllAlert() {

        return emailRepository.findAll();
    }

    public void sendEmail(String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new IllegalArgumentException("이메일 전송에 실패했습니다.");
        }
    }
}
