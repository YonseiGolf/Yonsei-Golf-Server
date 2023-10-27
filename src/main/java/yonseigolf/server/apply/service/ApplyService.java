package yonseigolf.server.apply.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yonseigolf.server.apply.dto.request.ApplicationRequest;
import yonseigolf.server.apply.dto.request.EmailAlertRequest;
import yonseigolf.server.apply.dto.response.ApplicationResponse;
import yonseigolf.server.apply.dto.response.SingleApplicationResult;
import yonseigolf.server.apply.entity.Application;
import yonseigolf.server.apply.entity.EmailAlarm;
import yonseigolf.server.apply.repository.ApplicationRepository;
import yonseigolf.server.apply.repository.EmailRepository;
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
                request.getName() +"님의 지원서가 정상적으로 제출되었습니다. \n\n" +
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
    public void updateDocumentPass(Long id, Boolean updatePass) {

        findById(id).updateDocumentPass(updatePass);
    }

    @Transactional
    public void updateFinalPass(Long id, boolean finalPass) {

        findById(id).updateFinalPass(finalPass);
    }

    @Transactional
    public void updateInterviewTime(Long id, LocalDateTime time) {

        findById(id).updateInterviewTime(time);
    }


    // TODO: ENUM 활용해서 코드 개선 가능할듯 보인다
    public void sendDocumentPassEmail() {
        List<Application> applications = findApplicationsByPassFail(true, null);

        applications.stream()
                .forEach(application -> emailService.sendEmail(application.getEmail(),
                        "안녕하세요. 연세대학교 골프동아리 결과 메일입니다.",
                        application.getName() +"님 서류 합격 축하드립니다. \n" +
                                "면접 일정은 추후 공지될 예정입니다. \n" +
                                "감사합니다."));
    }

    public void sendFinalPassEmail() {
        List<Application> applications = findApplicationsByPassFail(true, true);

        applications.stream()
                .forEach(application -> emailService.sendEmail(application.getEmail(),
                        "안녕하세요. 연세대학교 골프동아리 결과 메일입니다.",
                        application.getName() +"님 최종 합격 축하드립니다. \n" +
                                "추후 일정은 문자로 공지될 예정입니다. \n" +
                                "감사합니다."));
    }

    public void sendDocumentFailEmail() {
        List<Application> applications = findApplicationsByPassFail(false, null);

        applications.stream()
                .forEach(application -> emailService.sendEmail(application.getEmail(),
                        "안녕하세요. 연세대학교 골프동아리 결과 메일입니다.",
                        application.getName() + "님 연세골프에 지원해주셔서 감사합니다. \n\n\n" +
                                "안타깝게도 " + application.getName() + "님께 이번 연골 모집에서 합격의 소식을 전해드리지 못하게 되었습니다." +
                                application.getName() + "님의 뛰어난 열정에도 불구하고, 연세골프는 한정된 인원으로만 운영되는 만큼 아쉽게도 이런 소식을 전해드리게 됐습니다." +
                                "비록 이번 모집에서 " + application.getName() +"님과 함께하지 못하지만, 다음에 함께 할 수 있기를 바라겠습니다. \n\n" +
                                "바쁘신 와중에 지원해주셔서 감사합니다. \n\n" +
                                "연세 골프 운영진 드림")
                );
    }

    public void sendFinalFailEmail() {
        List<Application> applications = findApplicationsByPassFail(true, false);

        applications.stream()
                .forEach(application -> emailService.sendEmail(application.getEmail(),
                        "안녕하세요. 연세대학교 골프동아리 결과 메일입니다.",
                        application.getName() + "님 연세골프에 지원해주셔서 감사합니다. \n\n\n" +
                                "안타깝게도 " + application.getName() + "님께 이번 연골 모집에서 합격의 소식을 전해드리지 못하게 되었습니다." +
                                application.getName() + "님의 뛰어난 열정에도 불구하고, 연세골프는 한정된 인원으로만 운영되는 만큼 아쉽게도 이런 소식을 전해드리게 됐습니다." +
                                "비록 이번 모집에서 " + application.getName() +"님과 함께하지 못하지만, 다음에 함께 할 수 있기를 바라겠습니다. \n\n" +
                                "바쁘신 와중에 지원해주셔서 감사합니다. \n\n" +
                                "연세 골프 운영진 드림")
                );
    }

    private List<Application> findApplicationsByPassFail(Boolean docuemntPass, Boolean finalPass) {

        return applicationRepository.findApplicationsForEmail(docuemntPass, finalPass);
    }

    private Application findById(Long id) {

        return applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 지원서가 존재하지 않습니다. id"));
    }
}
