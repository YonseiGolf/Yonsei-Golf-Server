package yonseigolf.server.apply.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yonseigolf.server.apply.entity.RecruitmentPeriod;

import java.util.List;
import java.util.Optional;

public interface ApplyPeriodRepository extends JpaRepository<RecruitmentPeriod, Long> {

    Optional<RecruitmentPeriod> findTopByOrderBySemesterDesc();

    List<RecruitmentPeriod> findAllByOrderBySemesterDesc();
}
