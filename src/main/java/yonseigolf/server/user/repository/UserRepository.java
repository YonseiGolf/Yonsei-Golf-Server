package yonseigolf.server.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yonseigolf.server.user.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKakaoId(Long socialId);
}
