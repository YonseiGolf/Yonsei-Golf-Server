package yonseigolf.server.apply.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yonseigolf.server.apply.dto.request.RecruitmentPeriodRequest;
import yonseigolf.server.apply.dto.response.RecruitPeriodResponse;
import yonseigolf.server.apply.entity.RecruitmentPeriod;
import yonseigolf.server.apply.repository.ApplyPeriodRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplyPeriodService {

    private final ApplyPeriodRepository repository;

    @Autowired
    public ApplyPeriodService(ApplyPeriodRepository repository) {
        this.repository = repository;
    }

    public RecruitPeriodResponse getApplicationPeriod(long id) {

        RecruitmentPeriod recruitmentPeriod = findById(id);

        return toResponse(recruitmentPeriod);
    }

    public boolean getApplicationAvailability(LocalDate today, long periodId) {
        RecruitmentPeriod period = findById(periodId);

        return isApplicationAvailable(today, period);
    }

    public boolean getLatestApplicationAvailability(LocalDate today) {
        RecruitmentPeriod period = findLatestRecruitmentPeriod();

        return isApplicationAvailable(today, period);
    }

    private RecruitmentPeriod findById(long periodId) {

        return repository.findById(periodId).orElseThrow(
                () -> new IllegalArgumentException("해당 모집기간이 존재하지 않습니다."));
    }

    public RecruitPeriodResponse getLatestApplicationPeriod() {
        RecruitmentPeriod recruitmentPeriod = findLatestRecruitmentPeriod();

        return toResponse(recruitmentPeriod);
    }

    private RecruitmentPeriod findLatestRecruitmentPeriod() {
        return repository.findTopByOrderBySemesterDesc()
                .orElseThrow(() -> new IllegalArgumentException("등록된 모집기간이 존재하지 않습니다."));
    }

    private boolean isApplicationAvailable(LocalDate today, RecruitmentPeriod period) {
        return !today.isBefore(period.getStartDate()) && !today.isAfter(period.getEndDate());
    }

    public List<RecruitPeriodResponse> getAllRecruitmentPeriods() {
        return repository.findAllByOrderBySemesterDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createRecruitmentPeriod(RecruitmentPeriodRequest request) {
        RecruitmentPeriod recruitmentPeriod = RecruitmentPeriod.builder()
                .semester(request.getSemester())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .firstResultDate(request.getFirstResultDate())
                .finalResultDate(request.getFinalResultDate())
                .interviewStartDate(request.getInterviewStartDate())
                .interviewEndDate(request.getInterviewEndDate())
                .orientationDate(request.getOrientationDate())
                .build();

        repository.save(recruitmentPeriod);
    }

    @Transactional
    public void updateRecruitmentPeriod(Long id, RecruitmentPeriodRequest request) {
        RecruitmentPeriod recruitmentPeriod = findById(id);

        recruitmentPeriod.update(
                request.getSemester(),
                request.getStartDate(),
                request.getEndDate(),
                request.getFirstResultDate(),
                request.getFinalResultDate(),
                request.getInterviewStartDate(),
                request.getInterviewEndDate(),
                request.getOrientationDate()
        );
    }

    @Transactional
    public void deleteRecruitmentPeriod(Long id) {
        RecruitmentPeriod recruitmentPeriod = findById(id);
        repository.delete(recruitmentPeriod);
    }

    private RecruitPeriodResponse toResponse(RecruitmentPeriod recruitmentPeriod) {
        return RecruitPeriodResponse.builder()
                .id(recruitmentPeriod.getId())
                .semester(recruitmentPeriod.getSemester())
                .startDate(recruitmentPeriod.getStartDate())
                .endDate(recruitmentPeriod.getEndDate())
                .firstResultDate(recruitmentPeriod.getFirstResultDate())
                .interviewStartDate(recruitmentPeriod.getInterviewStartDate())
                .interviewEndDate(recruitmentPeriod.getInterviewEndDate())
                .finalResultDate(recruitmentPeriod.getFinalResultDate())
                .orientationDate(recruitmentPeriod.getOrientationDate())
                .build();
    }
}
