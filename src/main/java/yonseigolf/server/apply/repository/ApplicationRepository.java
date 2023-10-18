package yonseigolf.server.apply.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yonseigolf.server.apply.entity.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

}
