package yonseigolf.server.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import yonseigolf.server.user.dto.response.JwtTokenUser;

import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


@SpringBootTest
class JwtUtilServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("jwt token 생성 테스트")
    void testCreateToken() {
        // given
        JwtTokenUser loggedInUser = JwtTokenUser.builder()
                .id(1L)
                .build();
        Date expiredDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60); // 1시간 후 만료

        // when
        String token = jwtService.createToken(loggedInUser, expiredDate);

        // then
        assertAll(
                () -> assertThat(token).isNotNull(),
                () -> assertThat(token.split("\\.")).hasSize(3)
        );
    }

    @Test
    @DisplayName("refresh token 생성 테스트")
    void createRefreshTokenTest() {
        // given
        Long userId = 1L;
        Date expiredDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 14); // 2주 후 만료

        // when
        String token = jwtService.createRefreshToken(userId, expiredDate);

        // then
        assertAll(
                () -> assertThat(token).isNotNull(),
                () -> assertThat(token.split("\\.")).hasSize(3)
        );
    }

    @Test
    @DisplayName("토큰 시간 만료 테스트")
    void validateExpireTest() {
        // given
        JwtTokenUser loggedInUser = JwtTokenUser.builder()
                .id(1L)
                .build();
        Date expiredDate = new Date(new Date().getTime() - 1000 * 60 * 60); // 1시간 전
        String token = jwtService.createToken(loggedInUser, expiredDate);

        // when
        boolean results = jwtService.validateTokenIsExpired(token);

        // then
        assertThat(results).isFalse();
    }

    @Test
    @DisplayName("토큰 시간 만료 통과 테스트")
    void validateExpirePasstest() {
        // given
        JwtTokenUser loggedInUser = JwtTokenUser.builder()
                .id(1L)
                .build();
        Date expiredDate = new Date(new Date().getTime() + 1000 * 60 * 60);
        String token = jwtService.createToken(loggedInUser, expiredDate);

        // when
        boolean results = jwtService.validateTokenIsExpired(token);

        // then
        assertThat(results).isTrue();

    }

    @Test
    @DisplayName("토큰 조작 테스트")
    void validateManipulatedTest() {
        // given
        JwtTokenUser loggedInUser = JwtTokenUser.builder()
                .id(1L)
                .build();
        Date expiredDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60); // 1시간 후 만료
        String token = jwtService.createToken(loggedInUser, expiredDate) + 1;

        // when
        boolean result = jwtService.validateTokenIsManipulated(token);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("토큰 조작 통과 테스트")
    void validateManipulatedPassTest() {
        // given
        JwtTokenUser loggedInUser = JwtTokenUser.builder()
                .id(1L)
                .build();
        Date expiredDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60); // 1시간 후 만료
        String token = jwtService.createToken(loggedInUser, expiredDate);

        // when
        boolean result = jwtService.validateTokenIsManipulated(token);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("토큰에서 user 분리 테스트")
    void extractUserTest() {
        // given
        JwtTokenUser loggedInUser = JwtTokenUser.builder()
                .id(1L)
                .build();
        Date expiredDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60); // 1시간 후 만료
        String token = jwtService.createToken(loggedInUser, expiredDate);


        // when
        JwtTokenUser jwtTokenUser = jwtService.extractedUserFromToken(token, JwtTokenUser.class);

        // then
        assertAll(
                () -> assertThat(jwtTokenUser).isNotNull(),
                () -> assertThat(jwtTokenUser.getId()).isEqualTo(1L)
        );
    }
}
