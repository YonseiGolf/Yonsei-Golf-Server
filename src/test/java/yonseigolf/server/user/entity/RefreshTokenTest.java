package yonseigolf.server.user.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import yonseigolf.server.user.exception.RefreshTokenExpiredException;
import yonseigolf.server.user.service.JwtService;

import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@SpringBootTest
class RefreshTokenTest {

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("refreshToken 검증 테스트")
    void validateRefreshToken() {
        // given
        String refreshTokenStr = jwtService.createRefreshToken(1L, new Date(new Date().getTime() + 1000 * 60 * 60 * 24 * 7));

        RefreshToken refreshToken = RefreshToken.builder()
                .refreshToken(refreshTokenStr)
                .build();

        // when & then
        assertThatCode(() -> refreshToken.isBeforeExpired(jwtService))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("refreshToken 검증 테스트 - 토큰 만료됨")
    void validateRefreshToken_Expired() {
        // given
        String expiredTokenStr = jwtService.createRefreshToken(1L, new Date());
        RefreshToken expiredToken = RefreshToken.builder()
                .refreshToken(expiredTokenStr)
                .build();

        // when & then
        assertThatThrownBy(() -> expiredToken.isBeforeExpired(jwtService))
                .isInstanceOf(RefreshTokenExpiredException.class)
                .hasMessageContaining("Refresh Token이 만료되었습니다.");
    }
}
