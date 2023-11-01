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

        RecruitmentPeriod recruitmentPeriod = findById(id);

        return RecruitPeriodResponse.builder()
                .startDate(recruitmentPeriod.getStartDate())
                .endDate(recruitmentPeriod.getEndDate())
                .firstResultDate(recruitmentPeriod.getFirstResultDate())
                .interviewStartDate(recruitmentPeriod.getInterviewStartDate())
                .interviewEndDate(recruitmentPeriod.getInterviewEndDate())
                .finalResultDate(recruitmentPeriod.getFinalResultDate())
                .orientationDate(recruitmentPeriod.getOrientationDate())
                .build();
    }

    public boolean getApplicationAvailability(LocalDate today, long periodId) {
        RecruitmentPeriod period = findById(periodId);

        return !today.isBefore(period.getStartDate()) && !today.isAfter(period.getEndDate());
    }

    private RecruitmentPeriod findById(long periodId) {

        return repository.findById(periodId).orElseThrow(
                () -> new IllegalArgumentException("해당 모집기간이 존재하지 않습니다."));
    }
}
