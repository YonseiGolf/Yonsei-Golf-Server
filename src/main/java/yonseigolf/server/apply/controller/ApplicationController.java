package yonseigolf.server.apply.controller;

import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import yonseigolf.server.apply.dto.request.*;
import yonseigolf.server.apply.dto.response.ApplicationResponse;
import yonseigolf.server.apply.dto.response.ImageResponse;
import yonseigolf.server.apply.dto.response.InterviewTimeResponse;

import java.util.List;
import yonseigolf.server.apply.dto.response.RecruitPeriodResponse;
import yonseigolf.server.apply.dto.response.SingleApplicationResult;
import yonseigolf.server.apply.image.ImageService;
import yonseigolf.server.apply.service.ApplyPeriodService;
import yonseigolf.server.apply.service.ApplyService;
import yonseigolf.server.apply.service.InterviewTimeService;
import yonseigolf.server.util.CustomResponse;

import java.time.LocalDate;
import java.time.ZoneId;

@RestController
public class ApplicationController {

    private final ApplyService applicationService;
    private final ApplyPeriodService applyPeriodService;
    private final ImageService imageService;
    private final InterviewTimeService interviewTimeService;

    @Autowired
    public ApplicationController(ApplyService applicationService, ApplyPeriodService applyPeriodService,
                                  ImageService imageService, InterviewTimeService interviewTimeService) {

        this.applicationService = applicationService;
        this.applyPeriodService = applyPeriodService;
        this.imageService = imageService;
        this.interviewTimeService = interviewTimeService;
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

        RecruitPeriodResponse applicationPeriod = applyPeriodService.getLatestApplicationPeriod();
        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("연세골프 지원 기간 조회 성공", applicationPeriod));
    }

    @GetMapping("/application/recruit/{recruitId}/interview-times")
    public ResponseEntity<CustomResponse<List<InterviewTimeResponse>>> getInterviewTimesForApplicant(@PathVariable Long recruitId) {

        List<InterviewTimeResponse> interviewTimes = interviewTimeService.getInterviewTimes(recruitId);
        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("면접 시간 목록 조회 성공", interviewTimes));
    }

    @GetMapping("/admin/recruit")
    public ResponseEntity<CustomResponse<RecruitPeriodResponse>> getLatestRecruitmentPeriod() {

        RecruitPeriodResponse recruitPeriod = applyPeriodService.getLatestApplicationPeriod();
        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("모집 기간 조회 성공", recruitPeriod));
    }

    @GetMapping("/admin/recruits")
    public ResponseEntity<CustomResponse<List<RecruitPeriodResponse>>> getAllRecruitmentPeriods() {

        List<RecruitPeriodResponse> recruitPeriods = applyPeriodService.getAllRecruitmentPeriods();
        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("모집 기간 목록 조회 성공", recruitPeriods));
    }

    @PostMapping("/admin/recruit")
    public ResponseEntity<CustomResponse<Void>> createRecruitmentPeriod(@RequestBody RecruitmentPeriodRequest request) {

        applyPeriodService.createRecruitmentPeriod(request);
        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("모집 기간 등록 성공"));
    }

    @PatchMapping("/admin/recruit/{id}")
    public ResponseEntity<CustomResponse<Void>> updateRecruitmentPeriod(@PathVariable Long id,
                                                                        @RequestBody RecruitmentPeriodRequest request) {

        applyPeriodService.updateRecruitmentPeriod(id, request);
        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("모집 기간 수정 성공"));
    }

    @DeleteMapping("/admin/recruit/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteRecruitmentPeriod(@PathVariable Long id) {

        applyPeriodService.deleteRecruitmentPeriod(id);
        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("모집 기간 삭제 성공"));
    }

    @GetMapping("/admin/recruit/{recruitId}/interview-times")
    public ResponseEntity<CustomResponse<List<InterviewTimeResponse>>> getInterviewTimes(@PathVariable Long recruitId) {

        List<InterviewTimeResponse> interviewTimes = interviewTimeService.getInterviewTimes(recruitId);
        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("면접 시간 목록 조회 성공", interviewTimes));
    }

    @PostMapping("/admin/recruit/{recruitId}/interview-times")
    public ResponseEntity<CustomResponse<Void>> createInterviewTime(@PathVariable Long recruitId,
                                                                     @RequestBody InterviewTimeRequest request) {

        interviewTimeService.createInterviewTime(recruitId, request);
        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("면접 시간 등록 성공"));
    }

    @PatchMapping("/admin/interview-times/{interviewTimeId}")
    public ResponseEntity<CustomResponse<Void>> updateInterviewTime(@PathVariable Long interviewTimeId,
                                                                     @RequestBody InterviewTimeRequest request) {

        interviewTimeService.updateInterviewTime(interviewTimeId, request);
        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("면접 시간 수정 성공"));
    }

    @DeleteMapping("/admin/interview-times/{interviewTimeId}")
    public ResponseEntity<CustomResponse<Void>> deleteInterviewTime(@PathVariable Long interviewTimeId) {

        interviewTimeService.deleteInterviewTime(interviewTimeId);
        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("면접 시간 삭제 성공"));
    }

    @GetMapping("/application/availability")
    public ResponseEntity<CustomResponse<Boolean>> getApplicationAvailability() {
        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse(
                        "연세골프 지원 가능 여부 조회 성공",
                        applyPeriodService.getLatestApplicationAvailability(
                                LocalDate.now(ZoneId.of("Asia/Seoul")))));
    }

    @GetMapping("/admin/forms")
    public ResponseEntity<CustomResponse<Page<SingleApplicationResult>>> getApplicationResults(
            @RequestParam(required = false) Boolean documentPass,
            @RequestParam(required = false) Boolean finalPass,
            @RequestParam(required = true) int semester,
            Pageable pageable) {

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse(
                        "연세골프 지원서 조회 성공",
                        applicationService.getApplicationResults(documentPass, finalPass, semester, pageable)));
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
