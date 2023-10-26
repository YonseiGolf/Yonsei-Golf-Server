package yonseigolf.server.email.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import yonseigolf.server.apply.entity.EmailAlarm;
import yonseigolf.server.apply.repository.EmailRepository;

import java.util.ArrayList;
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

    public void sendApplyStartAlert() {
        List<EmailAlarm> allAlert = findAllAlert();

        allAlert.stream().forEach(alert -> {
            sendEmail(alert.getEmail(),
                    "연세대학교 골프동아리입니다.",
                    "연세대학교 골프동아리 모집이 시작되었습니다.\n " +
                            "https://yonseigolf.com/apply 에서 확인해주세요");
        });

        emailRepository.deleteAll();
    }

    private List<EmailAlarm> findAllAlert() {

        return emailRepository.findAll();
    }

    private void sendEmail(String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("이메일 전송 실패 : {}", to , e.getMessage());
        }
    }
}
