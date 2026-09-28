package yonseigolf.server.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yonseigolf.server.user.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
}
