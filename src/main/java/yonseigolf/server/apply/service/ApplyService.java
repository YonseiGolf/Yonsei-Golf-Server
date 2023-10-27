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

import java.time.LocalDateTime;


@Service
public class ApplyService {

    private final ApplicationRepository applicationRepository;
    private final EmailRepository emailRepository;

    @Autowired
    public ApplyService(ApplicationRepository applicationRepository, EmailRepository emailRepository) {

        this.applicationRepository = applicationRepository;
        this.emailRepository = emailRepository;
    }

    public void apply(ApplicationRequest request) {

        applicationRepository.save(Application.of(request));
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

    private Application findById(Long id) {

        return applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 지원서가 존재하지 않습니다. id"));
    }
}
