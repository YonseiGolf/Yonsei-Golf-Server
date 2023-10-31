package yonseigolf.server.apply.controller;

import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import yonseigolf.server.apply.dto.request.*;
import yonseigolf.server.apply.dto.response.ApplicationResponse;
import yonseigolf.server.apply.dto.response.ImageResponse;
import yonseigolf.server.apply.dto.response.SingleApplicationResult;
import yonseigolf.server.apply.image.ImageService;
import yonseigolf.server.apply.service.ApplyPeriodService;
import yonseigolf.server.apply.service.ApplyService;
import yonseigolf.server.util.CustomResponse;

import java.time.LocalDate;

@Controller
public class ApplicationController {

    private final ApplyService applicationService;
    private final ApplyPeriodService applyPeriodService;
    private final ImageService imageService;

    @Autowired
    public ApplicationController(ApplyService applicationService, ApplyPeriodService applyPeriodService, ImageService imageService) {

        this.applicationService = applicationService;
        this.applyPeriodService = applyPeriodService;
        this.imageService = imageService;
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

        final long defaultId = 1L;

        return ResponseEntity
                .ok()
                .body(new CustomResponse(
                        "success",
                        200,
                        "연세골프 지원 기간 조회 성공",
                        applyPeriodService.getApplicationPeriod(defaultId)
                ));
    }

    @GetMapping("/application/availability")
    public ResponseEntity<CustomResponse<Boolean>> getApplicationAvaliability() {

        final long defaultId = 1L;

        return ResponseEntity
                .ok()
                .body(new CustomResponse<>(
                        "success",
                        200,
                        "연세골프 지원 가능 여부 조회 성공",
                        applyPeriodService.getApplicationAvailability(LocalDate.now(), defaultId)
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

    @PostMapping("/admin/forms/results")
    public ResponseEntity<CustomResponse> sendEmailNotification(@RequestBody ResultNotification request) {

        applicationService.sendEmailNotification(request.isDocumentPass(), request.getFinalPass());

        return ResponseEntity
                .ok()
                .body(new CustomResponse(
                        "success",
                        200,
                        "연세골프 지원서 결과 이메일 전송 성공"
                ));
    }

    @PostMapping("/apply/forms/image")
    public ResponseEntity<CustomResponse<ImageResponse>> uploadImage(@RequestPart("image") MultipartFile image) {

        String imageUrl = imageService.uploadImage(image, RandomString.make(10));

        return ResponseEntity
                    .ok()
                    .body(new CustomResponse(
                            "success",
                            200,
                            "연세골프 지원서 이미지 업로드 성공",
                            new ImageResponse(imageUrl)
                    ));
    }
}
