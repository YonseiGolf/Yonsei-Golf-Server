package yonseigolf.server.email.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import yonseigolf.server.apply.entity.EmailAlarm;
import yonseigolf.server.apply.repository.EmailRepository;

import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailRepository emailRepository;

    @Autowired
    public EmailService(JavaMailSender mailSender, EmailRepository emailRepository) {

        this.mailSender = mailSender;
        this.emailRepository = emailRepository;
    }

    public void sendSimpleMessage() {
        List<EmailAlarm> allAlert = findAllAlert();

        allAlert.stream().forEach(alert -> {
            sendEmail(alert.getEmail(), "연세대학교 골프동아리 결과입니다.", "결과입니다.");
        });
    }

    private List<EmailAlarm> findAllAlert() {

        return emailRepository.findAll();
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
