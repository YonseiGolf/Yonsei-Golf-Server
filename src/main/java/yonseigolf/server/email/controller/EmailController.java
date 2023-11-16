package yonseigolf.server.email.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import yonseigolf.server.email.dto.response.AllWaitingEmail;
import yonseigolf.server.email.service.EmailService;
import yonseigolf.server.util.CustomResponse;

@Controller
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {

        this.emailService = emailService;
    }

    @GetMapping("/admin/email/apply-start-email")
    public ResponseEntity<CustomResponse<AllWaitingEmail>> findAllWaitingEmail() {

        AllWaitingEmail allWaitingEmail = emailService.findAllWaitingEmail();
        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("지원 시작 이메일 전송 대기 목록 조회 성공", allWaitingEmail));
    }

    @PostMapping("/admin/email/apply-start-email")
    public ResponseEntity<CustomResponse<Void>> sendApplyStartAlert() {

        emailService.sendApplyStartAlert();

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("지원 시작 이메일 전송 성공"));
    }
}
