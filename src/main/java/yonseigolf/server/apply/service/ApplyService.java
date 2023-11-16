package yonseigolf.server.apply.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yonseigolf.server.apply.dto.request.ApplicationRequest;
import yonseigolf.server.apply.dto.request.EmailAlertRequest;
import yonseigolf.server.apply.dto.request.UpdatePassRequest;
import yonseigolf.server.apply.dto.response.ApplicationResponse;
import yonseigolf.server.apply.dto.response.SingleApplicationResult;
import yonseigolf.server.apply.entity.Application;
import yonseigolf.server.apply.entity.EmailAlarm;
import yonseigolf.server.apply.repository.ApplicationRepository;
import yonseigolf.server.apply.repository.EmailRepository;
import yonseigolf.server.email.dto.NotificationType;
import yonseigolf.server.email.service.EmailService;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class ApplyService {

    private final ApplicationRepository applicationRepository;
    private final EmailRepository emailRepository;
    private final EmailService emailService;

    @Autowired
    public ApplyService(ApplicationRepository applicationRepository, EmailRepository emailRepository, EmailService emailService) {

        this.applicationRepository = applicationRepository;
        this.emailRepository = emailRepository;
        this.emailService = emailService;
    }

    public void apply(ApplicationRequest request) {

        applicationRepository.save(Application.of(request));
        emailService.sendEmail(request.getEmail(),
                "안녕하세요. 연세골프입니다.\n\n",
                request.getName() + "님의 지원서가 정상적으로 제출되었습니다. \n\n" +
                        "서류 합격 여부는 추후 이메일로 공지될 예정입니다. \n\n" +
                        "감사합니다."
        );
    }

    public void emailAlarm(EmailAlertRequest request) {

        emailRepository.save(EmailAlarm.of(request));
    }

    public Page<SingleApplicationResult> getApplicationResults(Boolean documentPass, Boolean finalPass, Pageable pageable) {

        return applicationRepository.getApplicationResults(documentPass, finalPass, pageable);
    }

    public ApplicationResponse getApplication(Long id) {

        return ApplicationResponse.fromApplication(findById(id));
    }

    @Transactional
    public void updatePass(Long id, UpdatePassRequest request) {

        findById(id).updatePass(request);
    }

//    @Transactional
//    public void updateDocumentPass(Long id, Boolean updatePass) {
//
//        findById(id).updateDocumentPass(updatePass);
//    }
//
//    @Transactional
//    public void updateFinalPass(Long id, Boolean finalPass) {
//
//        findById(id).updateFinalPass(finalPass);
//    }

    @Transactional
    public void updateInterviewTime(Long id, LocalDateTime time) {

        findById(id).updateInterviewTime(time);
    }

    public void sendEmailNotification(boolean isDocumentPass, Boolean isFinalPass) {

        final NotificationType type = getNotificationType(isDocumentPass, isFinalPass);
        final String subject = "안녕하세요. 연세대학교 골프동아리 결과 메일입니다.";

        findApplicationsByPassFail(isDocumentPass, isFinalPass)
                .forEach(application -> {
                    final String message = type.generateMessage(application.getName());
                    emailService.sendEmail(application.getEmail(), subject, message);
                });
    }

    private NotificationType getNotificationType(boolean isDocumentPass, Boolean isFinalPass) {

        if (isDocumentPass && isFinalPass == null) {
            return NotificationType.DOCUMENT_PASS;
        }
        if (isDocumentPass && isFinalPass) {
            return NotificationType.FINAL_PASS;
        }
        return NotificationType.FAIL;
    }

    private List<Application> findApplicationsByPassFail(Boolean docuemntPass, Boolean finalPass) {

        return applicationRepository.findApplicationsForEmail(docuemntPass, finalPass);
    }

    private Application findById(Long id) {

        return applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 지원서가 존재하지 않습니다. id"));
    }
}
