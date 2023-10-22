package yonseigolf.server.apply.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yonseigolf.server.apply.dto.request.ApplicationRequest;
import yonseigolf.server.apply.dto.request.EmailRequest;
import yonseigolf.server.apply.entity.Application;
import yonseigolf.server.apply.entity.EmailAlarm;
import yonseigolf.server.apply.repository.ApplicationRepository;
import yonseigolf.server.apply.repository.EmailRepository;

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

    public void emailAlarm(EmailRequest request) {

        emailRepository.save(EmailAlarm.of(request));
    }
}
