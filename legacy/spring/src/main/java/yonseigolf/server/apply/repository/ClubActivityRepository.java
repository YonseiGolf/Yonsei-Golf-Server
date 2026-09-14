package yonseigolf.server.apply.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yonseigolf.server.apply.entity.ClubActivity;

public interface ClubActivityRepository extends JpaRepository<ClubActivity, Long> {

}
