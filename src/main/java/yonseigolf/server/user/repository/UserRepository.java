package yonseigolf.server.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yonseigolf.server.user.entity.User;
import yonseigolf.server.user.entity.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKakaoId(Long socialId);

    Optional<User> findLeaderByRole(UserRole role);

    List<User> findAssistantLeadersByRole(UserRole role);
}
