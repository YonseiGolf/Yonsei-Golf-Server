package yonseigolf.server.apply.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yonseigolf.server.apply.entity.ApplicationResultLog;
import yonseigolf.server.email.dto.NotificationType;

public interface ApplicationResultLogRepository extends JpaRepository<ApplicationResultLog, Long> {
    boolean existsByApplicationIdAndNotificationType(
        Long applicationId,
        NotificationType notificationType
    );
}
