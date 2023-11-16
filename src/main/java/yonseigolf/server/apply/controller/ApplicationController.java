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
import yonseigolf.server.apply.dto.response.RecruitPeriodResponse;
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
    public ResponseEntity<CustomResponse<Void>> apply(@RequestBody ApplicationRequest request) {

        applicationService.apply(request);

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("연세골프 지원서 제출 성공"));
    }

    @PostMapping("/application/emailAlarm")
    public ResponseEntity<CustomResponse<Void>> emailAlarm(@RequestBody EmailAlertRequest request) {

        applicationService.emailAlarm(request);

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("연세골프 지원서 이메일 알림 설정 성공"));
    }

    @GetMapping("/application/recruit")
    public ResponseEntity<CustomResponse<RecruitPeriodResponse>> getApplicationPeriod() {

        final long defaultId = 1L;

        RecruitPeriodResponse applicationPeriod = applyPeriodService.getApplicationPeriod(defaultId);
        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("연세골프 지원 기간 조회 성공", applicationPeriod));
    }

    @GetMapping("/application/availability")
    public ResponseEntity<CustomResponse<Boolean>> getApplicationAvaliability() {

        final long defaultId = 1L;

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse(
                        "연세골프 지원 가능 여부 조회 성공",
                        applyPeriodService.getApplicationAvailability(LocalDate.now(), defaultId)));
    }

    @GetMapping("/admin/forms")
    public ResponseEntity<CustomResponse<Page<SingleApplicationResult>>> getApplicationResults(
            @RequestParam(required = false) Boolean documentPass,
            @RequestParam(required = false) Boolean finalPass,
            Pageable pageable) {

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse(
                        "연세골프 지원서 조회 성공",
                        applicationService.getApplicationResults(documentPass, finalPass, pageable)));
    }

    @GetMapping("/admin/forms/{id}")
    public ResponseEntity<CustomResponse<ApplicationResponse>> getApplication(@PathVariable Long id) {

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse(
                        "연세골프 지원서 조회 성공",
                        applicationService.getApplication(id)));
    }

    @PatchMapping("/admin/forms/{id}/pass")
    public ResponseEntity<CustomResponse<Void>> updatePass(@PathVariable Long id, @RequestBody UpdatePassRequest pass) {

        applicationService.updatePass(id, pass);

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("연세골프 지원서 합격 여부 수정 성공"));
    }

    @PatchMapping("/admin/forms/{id}/interviewTime")
    public ResponseEntity<CustomResponse<Void>> updateInterviewTime(@PathVariable Long id, @RequestBody UpdateInterviewTimeRequest time) {

        applicationService.updateInterviewTime(id, time.getTime());

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("연세골프 지원서 면접 시간 수정 성공"));
    }

    @PostMapping("/admin/forms/results")
    public ResponseEntity<CustomResponse<Void>> sendEmailNotification(@RequestBody ResultNotification request) {

        applicationService.sendEmailNotification(request.isDocumentPass(), request.getFinalPass());

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("연세골프 지원서 결과 이메일 발송 성공"));
    }

    @PostMapping("/apply/forms/image")
    public ResponseEntity<CustomResponse<ImageResponse>> uploadImage(@RequestPart("image") MultipartFile image) {

        String imageUrl = imageService.uploadImage(image, RandomString.make(10));

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse(
                        "연세골프 지원서 사진 업로드 성공",
                        new ImageResponse(imageUrl)));
    }
}
