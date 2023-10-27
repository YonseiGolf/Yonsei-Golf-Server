package yonseigolf.server.apply.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import yonseigolf.server.apply.dto.request.*;
import yonseigolf.server.apply.dto.response.ApplicationResponse;
import yonseigolf.server.apply.dto.response.SingleApplicationResult;
import yonseigolf.server.apply.service.ApplyPeriodService;
import yonseigolf.server.apply.service.ApplyService;
import yonseigolf.server.util.CustomResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @GetMapping("/admin/forms")
    public ResponseEntity<CustomResponse<Page<SingleApplicationResult>>> getApplicationResults(@RequestParam(required = false) Boolean documentPass,
                                                                                               @RequestParam(required = false) Boolean finalPass,
                                                                                               Pageable pageable) {

        System.out.println("documentPass = " + documentPass);
        System.out.println("finalPass = " + finalPass);

        return ResponseEntity
                .ok()
                .body(new CustomResponse(
                        "success",
                        200,
                        "연세골프 지원서 조회 성공",
                        applicationService.getApplicationResults(documentPass, finalPass, pageable)
                ));
    }

    @GetMapping("/admin/forms/{id}")
    public ResponseEntity<CustomResponse<ApplicationResponse>> getApplication(@PathVariable Long id) {

        return ResponseEntity
                .ok()
                .body(new CustomResponse(
                        "success",
                        200,
                        "연세골프 지원서 조회 성공",
                        applicationService.getApplication(id)
                ));
    }

    @PatchMapping("/admin/forms/{id}/documentPass")
    public ResponseEntity<CustomResponse> updateDocumentPass(@PathVariable Long id, @RequestBody DocumentPassRequest documentPass) {

        applicationService.updateDocumentPass(id, documentPass.isDocumentPass());

        return ResponseEntity
                .ok()
                .body(new CustomResponse(
                        "success",
                        200,
                        "연세골프 지원서 서류 합격 수정 성공"
                ));
    }

    @PatchMapping("/admin/forms/{id}/finalPass")
    public ResponseEntity<CustomResponse> updateFinalPass(@PathVariable Long id, @RequestBody FinalPassRequest finalPass) {

        applicationService.updateFinalPass(id, finalPass.isFinalPass());

        return ResponseEntity
                .ok()
                .body(new CustomResponse(
                        "success",
                        200,
                        "연세골프 지원서 최종 합격 수정 성공"
                ));
    }

    @PatchMapping("/admin/forms/{id}/interviewTime")
    public ResponseEntity<CustomResponse> updateInterviewTime(@PathVariable Long id, @RequestBody UpdateInterviewTimeRequest time) {

        applicationService.updateInterviewTime(id, time.getTime());

        return ResponseEntity
                .ok()
                .body(new CustomResponse(
                        "success",
                        200,
                        "연세골프 지원서 면접 시간 수정 성공"
                ));
    }

    @PostMapping("/admin/forms/documentPassEmail")
    public ResponseEntity<CustomResponse> sendDocumentPassEmail() {

        applicationService.sendDocumentPassEmail();

        return ResponseEntity
                .ok()
                .body(new CustomResponse(
                        "success",
                        200,
                        "연세골프 지원서 서류 합격자 이메일 전송 성공"
                ));
    }

    @PostMapping("/admin/forms/finalPassEmail")
    public ResponseEntity<CustomResponse> sendFinalPassEmail() {

        applicationService.sendFinalPassEmail();

        return ResponseEntity
                .ok()
                .body(new CustomResponse(
                        "success",
                        200,
                        "연세골프 지원서 최종 합격자 이메일 전송 성공"
                ));
    }

    @PostMapping("/admin/forms/documentFailEmail")
    public ResponseEntity<CustomResponse> sendDocumentFailEmail() {

        applicationService.sendDocumentFailEmail();

        return ResponseEntity
                .ok()
                .body(new CustomResponse(
                        "success",
                        200,
                        "연세골프 지원서 서류 불합격자 이메일 전송 성공"
                ));
    }

    @PostMapping("/admin/forms/finalFailEmail")
    public ResponseEntity<CustomResponse> sendFinalFailEmail() {

        applicationService.sendFinalFailEmail();

        return ResponseEntity
                .ok()
                .body(new CustomResponse(
                        "success",
                        200,
                        "연세골프 지원서 최종 불합격자 이메일 전송 성공"
                ));
    }
}
