package yonseigolf.server.apply.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yonseigolf.server.apply.dto.response.RecruitPeriodResponse;
import yonseigolf.server.apply.entity.RecruitmentPeriod;
import yonseigolf.server.apply.repository.ApplyPeriodRepository;

import java.time.LocalDate;

@Service
public class ApplyPeriodService {

    private final ApplyPeriodRepository repository;

    @Autowired
    public ApplyPeriodService(ApplyPeriodRepository repository) {
        this.repository = repository;
    }

    public RecruitPeriodResponse getApplicationPeriod(long id) {

        return RecruitPeriodResponse.builder()
                .startDate(repository.findById(id).get().getStartDate())
                .endDate(repository.findById(id).get().getEndDate())
                .firstResultDate(repository.findById(id).get().getFirstResultDate())
                .interviewStartDate(repository.findById(id).get().getInterviewStartDate())
                .interviewEndDate(repository.findById(id).get().getInterviewEndDate())
                .finalResultDate(repository.findById(id).get().getFinalResultDate())
                .orientationDate(repository.findById(id).get().getOrientationDate())
                .build();
    }

    public boolean getApplicationAvailability(LocalDate today, long periodId) {
        RecruitmentPeriod period = repository.getOne(periodId);

        return !today.isBefore(period.getStartDate()) && !today.isAfter(period.getEndDate());
    }
}
