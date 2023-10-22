package yonseigolf.server.apply.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yonseigolf.server.apply.entity.RecruitmentPeriod;

public interface ApplyPeriodRepository extends JpaRepository<RecruitmentPeriod, Long> {
}
