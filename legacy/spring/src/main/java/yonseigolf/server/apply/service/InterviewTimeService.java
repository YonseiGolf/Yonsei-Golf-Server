package yonseigolf.server.apply.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yonseigolf.server.apply.dto.request.InterviewTimeRequest;
import yonseigolf.server.apply.dto.response.InterviewTimeResponse;
import yonseigolf.server.apply.entity.InterviewTime;
import yonseigolf.server.apply.entity.RecruitmentPeriod;
import yonseigolf.server.apply.repository.ApplyPeriodRepository;
import yonseigolf.server.apply.repository.InterviewTimeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterviewTimeService {

    private final InterviewTimeRepository interviewTimeRepository;
    private final ApplyPeriodRepository applyPeriodRepository;

    @Autowired
    public InterviewTimeService(InterviewTimeRepository interviewTimeRepository,
                                 ApplyPeriodRepository applyPeriodRepository) {
        this.interviewTimeRepository = interviewTimeRepository;
        this.applyPeriodRepository = applyPeriodRepository;
    }

    public List<InterviewTimeResponse> getInterviewTimes(Long recruitmentPeriodId) {
        return interviewTimeRepository.findByRecruitmentPeriodIdOrderByInterviewDateTimeAsc(recruitmentPeriodId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createInterviewTime(Long recruitmentPeriodId, InterviewTimeRequest request) {
        RecruitmentPeriod recruitmentPeriod = findRecruitmentPeriodById(recruitmentPeriodId);

        InterviewTime interviewTime = InterviewTime.builder()
                .recruitmentPeriod(recruitmentPeriod)
                .interviewDateTime(request.getInterviewDateTime())
                .build();

        interviewTimeRepository.save(interviewTime);
    }

    @Transactional
    public void updateInterviewTime(Long interviewTimeId, InterviewTimeRequest request) {
        InterviewTime interviewTime = findById(interviewTimeId);
        interviewTime.update(request.getInterviewDateTime());
    }

    @Transactional
    public void deleteInterviewTime(Long interviewTimeId) {
        InterviewTime interviewTime = findById(interviewTimeId);
        interviewTimeRepository.delete(interviewTime);
    }

    private InterviewTime findById(Long id) {
        return interviewTimeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 면접 시간이 존재하지 않습니다."));
    }

    private RecruitmentPeriod findRecruitmentPeriodById(Long id) {
        return applyPeriodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 모집기간이 존재하지 않습니다."));
    }

    private InterviewTimeResponse toResponse(InterviewTime interviewTime) {
        return InterviewTimeResponse.builder()
                .id(interviewTime.getId())
                .interviewDateTime(interviewTime.getInterviewDateTime())
                .build();
    }
}
