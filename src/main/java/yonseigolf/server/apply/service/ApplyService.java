package yonseigolf.server.apply.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yonseigolf.server.apply.dto.request.ApplicationRequest;
import yonseigolf.server.apply.dto.request.EmailAlertRequest;
import yonseigolf.server.apply.dto.request.EmailConfirmationRequest;
import yonseigolf.server.apply.dto.request.UpdatePassRequest;
import yonseigolf.server.apply.dto.response.ApplicationResponse;
import yonseigolf.server.apply.dto.response.SingleApplicationResult;
import yonseigolf.server.apply.entity.Application;
import yonseigolf.server.apply.entity.ApplicationResultLog;
import yonseigolf.server.apply.entity.ClubActivity;
import yonseigolf.server.apply.entity.EmailAlarm;
import yonseigolf.server.apply.entity.InterviewTime;
import yonseigolf.server.apply.event.AppliedEvent;
import yonseigolf.server.apply.image.ImageService;
import yonseigolf.server.apply.repository.ApplicationRepository;
import yonseigolf.server.apply.repository.ApplicationResultLogRepository;
import yonseigolf.server.apply.repository.ClubActivityRepository;
import yonseigolf.server.apply.repository.EmailRepository;
import yonseigolf.server.apply.repository.InterviewTimeRepository;
import yonseigolf.server.email.dto.NotificationType;
import yonseigolf.server.email.service.EmailService;
import yonseigolf.server.event.Events;


@Service
@Slf4j
@RequiredArgsConstructor
public class ApplyService {

    private final ApplicationRepository applicationRepository;
    private final EmailRepository emailRepository;
    private final EmailService emailService;
    private final ApplicationResultLogRepository applicationResultLogRepository;
    private final ClubActivityRepository clubActivityRepository;
    private final InterviewTimeRepository interviewTimeRepository;
    private final ImageService imageService;

    @Transactional
    public void apply(ApplicationRequest request) {

        Application application = applicationRepository.save(Application.of(request));
        request.getActivityClubs().forEach(
            activity -> {
                clubActivityRepository.save(ClubActivity.builder()
                    .applicationId(application.getId())
                    .clubName(activity.getClubName())
                    .startDate(activity.getStartDate())
                    .endDate(activity.getEndDate())
                    .role(activity.getRole())
                    .build());
            }
        );

        if (request.getAvailableInterviewTimeIds() != null && !request.getAvailableInterviewTimeIds().isEmpty()) {
            List<InterviewTime> interviewTimes = interviewTimeRepository.findAllById(request.getAvailableInterviewTimeIds());
            application.setAvailableInterviewTimes(interviewTimes);
        }

        // TODO : async로 변경 필요
        Events.raise(new AppliedEvent(
            request.getEmail(), request.getName(), application.getId()
        ));
    }

    public void emailAlarm(EmailAlertRequest request) {
        emailRepository.save(EmailAlarm.of(request));
    }

    public void sendEmailConfirmation(EmailConfirmationRequest request) {
        emailService.sendApplicationEmailConfirmation(request.getEmail());
    }

    public Page<SingleApplicationResult> getApplicationResults(Boolean documentPass,
        Boolean finalPass, int semester, Pageable pageable) {

        return applicationRepository.getApplicationResults(documentPass, finalPass, semester,
                pageable)
            .map(result -> result.withPhotoUrl(
                imageService.resolveImageUrl(result.getPhotoKey(), result.getPhoto())));
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplication(Long id) {

        Application application = findById(id);
        String photoUrl = imageService.resolveImageUrl(
            application.getPhotoKey(), application.getPhoto());

        return ApplicationResponse.fromApplication(application, photoUrl);
    }

    @Transactional
    public void updatePass(Long id, UpdatePassRequest request) {

        findById(id).updatePass(request);
    }

    @Transactional
    public void updateInterviewTime(Long id, LocalDateTime time) {

        findById(id).updateInterviewTime(time);
    }

    public void sendEmailNotification(boolean isDocumentPass, Boolean isFinalPass) {
        // document pass, final pass 결과 저장
        // id, 기수, userName, 학과, (document_pass, final_pass, fail)로 전송된 적 있는지 감지
        final NotificationType type = NotificationType.decideNotificationType(isDocumentPass,
            isFinalPass);
        final String subject = "안녕하세요. 연세대학교 골프동아리 결과 메일입니다.";

        findApplicationsByPassFail(isDocumentPass, isFinalPass)
            .forEach(application -> {
                boolean exists = applicationResultLogRepository.existsByApplicationIdAndNotificationType(
                    application.getId(), type);

                if (exists) {
                    return;
                }
                log.info("ApplyService::SendEmailNotification - send email to {}",
                    application.getEmail());
                ApplicationResultLog applicationResultLog = ApplicationResultLog.builder()
                    .notificationType(type)
                    .applicationId(application.getId())
                    .sentAt(LocalDateTime.now())
                    .build();
                applicationResultLogRepository.save(applicationResultLog);

                final String message = type.generateMessage(application.getName());
                emailService.sendEmail(application.getEmail(), subject, message);
            });
    }

    private List<Application> findApplicationsByPassFail(Boolean docuemntPass, Boolean finalPass) {

        return applicationRepository.findApplicationsForEmail(docuemntPass, finalPass);
    }

    private Application findById(Long id) {

        return applicationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 지원서가 존재하지 않습니다. id"));
    }
}
