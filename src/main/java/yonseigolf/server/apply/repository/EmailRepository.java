package yonseigolf.server.apply.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yonseigolf.server.apply.entity.EmailAlarm;

public interface EmailRepository extends JpaRepository<EmailAlarm, Long> {
}
