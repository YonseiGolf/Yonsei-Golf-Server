package yonseigolf.server.apply.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import yonseigolf.server.apply.entity.EmailAlarm;

public interface EmailRepository extends JpaRepository<EmailAlarm, Long> {
    List<EmailAlarm> findAllBySentAtIsNull();
    List<EmailAlarm> findAllBySemester(int semester);
}
