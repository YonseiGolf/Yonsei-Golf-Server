package yonseigolf.server.apply.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import yonseigolf.server.apply.dto.request.ApplicationRequest;
import yonseigolf.server.apply.dto.request.EmailAlertRequest;
import yonseigolf.server.apply.dto.response.SingleApplicationResult;
import yonseigolf.server.apply.service.ApplyPeriodService;
import yonseigolf.server.apply.service.ApplyService;
import yonseigolf.server.util.CustomResponse;

import java.time.LocalDate;

@Controller
public class ApplicationController {

    private final ApplyService applicationService;
    private final ApplyPeriodService applyPeriodService;

    @Autowired
    public ApplicationController(ApplyService applicationService, ApplyPeriodService applyPeriodService) {

        this.applicationService = applicationService;
        this.applyPeriodService = applyPeriodService;
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
    public ResponseEntity<CustomResponse> emailAlarm(@RequestBody EmailAlertRequest request) {

        applicationService.emailAlarm(request);

        return ResponseEntity
                .ok()
                .body(new CustomResponse(
                        "success",
                        200,
                        "연세골프 이메일 알림 신청 성공"
                ));
    }

    @GetMapping("/application/recruit")
    public ResponseEntity<CustomResponse> getApplicationPeriod() {

        return ResponseEntity
                .ok()
                .body(new CustomResponse(
                        "success",
                        200,
                        "연세골프 지원 기간 조회 성공",
                        applyPeriodService.getApplicationPeriod()
                ));
    }

    @GetMapping("/application/availability")
    public ResponseEntity<CustomResponse<Boolean>> getApplicationAvaliability() {

        return ResponseEntity
                .ok()
                .body(new CustomResponse<>(
                        "success",
                        200,
                        "연세골프 지원 가능 여부 조회 성공",
                        applyPeriodService.getApplicationAvailability(LocalDate.now())
                ));
    }

    @GetMapping("/admin/application")
    public ResponseEntity<CustomResponse<Page<SingleApplicationResult>>> getApplicationResults(@RequestParam(required = false) Boolean documentPass,
                                                                                               @RequestParam(required = false) Boolean finalPass,
                                                                                               Pageable pageable) {

        return ResponseEntity
                .ok()
                .body(new CustomResponse(
                        "success",
                        200,
                        "연세골프 지원서 조회 성공",
                        applicationService.getApplicationResults(documentPass, finalPass, pageable)
                ));
    }
}
