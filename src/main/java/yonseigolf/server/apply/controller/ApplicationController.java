package yonseigolf.server.apply.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import yonseigolf.server.apply.dto.request.ApplicationRequest;
import yonseigolf.server.apply.dto.request.EmailRequest;
import yonseigolf.server.apply.service.ApplyService;
import yonseigolf.server.util.CustomResponse;

@Controller
public class ApplicationController {

    private final ApplyService applicationService;

    public ApplicationController(ApplyService applicationService) {

        this.applicationService = applicationService;
    }

    @PostMapping("/application")
    public ResponseEntity<CustomResponse> apply(@RequestBody ApplicationRequest request) {

        applicationService.apply(request);

        return ResponseEntity
                .ok()
                .body(new CustomResponse(
                        "success",
                        200,
                        "연세골프 지원서 제출 성공"
                ));
    }

    @PostMapping("/application/emailAlarm")
    public ResponseEntity<CustomResponse> emailAlarm(@RequestBody EmailRequest request) {

        applicationService.emailAlarm(request);

        return ResponseEntity
                .ok()
                .body(new CustomResponse(
                        "success",
                        200,
                        "연세골프 이메일 알림 신청 성공"
                ));
    }
}
