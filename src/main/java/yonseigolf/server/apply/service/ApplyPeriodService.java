package yonseigolf.server.apply.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yonseigolf.server.apply.dto.response.RecruitPeriodResponse;
import yonseigolf.server.apply.repository.ApplyPeriodRepository;

@Service
public class ApplyPeriodService {

    private final ApplyPeriodRepository repository;

    @Autowired
    public ApplyPeriodService(ApplyPeriodRepository repository) {
        this.repository = repository;
    }

    public RecruitPeriodResponse getApplicationPeriod() {

        return RecruitPeriodResponse.builder()
                .startDate(repository.getOne(1L).getStartDate())
                .endDate(repository.getOne(1L).getEndDate())
                .firstResultDate(repository.getOne(1L).getFirstResultDate())
                .interviewStartDate(repository.getOne(1L).getInterviewStartDate())
                .interviewEndDate(repository.getOne(1L).getInterviewEndDate())
                .finalResultDate(repository.getOne(1L).getFinalResultDate())
                .orientationDate(repository.getOne(1L).getOrientationDate())
                .build();
    }
}
