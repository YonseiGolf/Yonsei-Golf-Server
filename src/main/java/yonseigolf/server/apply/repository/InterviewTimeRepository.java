package yonseigolf.server.apply.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yonseigolf.server.apply.entity.InterviewTime;

import java.util.List;

public interface InterviewTimeRepository extends JpaRepository<InterviewTime, Long> {

    List<InterviewTime> findByRecruitmentPeriodIdOrderByInterviewDateTimeAsc(Long recruitmentPeriodId);
}
