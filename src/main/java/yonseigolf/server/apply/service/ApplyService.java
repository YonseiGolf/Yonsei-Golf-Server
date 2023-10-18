package yonseigolf.server.apply.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yonseigolf.server.apply.dto.request.ApplicationRequest;
import yonseigolf.server.apply.entity.Application;
import yonseigolf.server.apply.repository.ApplicationRepository;

@Service
public class ApplyService {

    private final ApplicationRepository applicationRepository;

    @Autowired
    public ApplyService(ApplicationRepository applicationRepository) {

        this.applicationRepository = applicationRepository;
    }

    public void apply(ApplicationRequest request) {

        applicationRepository.save(Application.of(request));
    }
}
