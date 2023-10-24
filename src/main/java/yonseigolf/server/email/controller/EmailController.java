package yonseigolf.server.email.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import yonseigolf.server.email.service.EmailService;
import yonseigolf.server.util.CustomResponse;

@Controller
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {

        this.emailService = emailService;
    }

    @PostMapping("/email/apply-start-email")
    public ResponseEntity<CustomResponse> sendApplyStartAlert() {

        emailService.sendApplyStartAlert();

        return ResponseEntity
                .ok()
                .body(new CustomResponse(
                        "success",
                        200,
                        "이메일 전송 성공"
                ));
    }
}
