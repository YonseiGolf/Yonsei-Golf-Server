package yonseigolf.server.user.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import yonseigolf.server.user.service.JwtService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

class UserTest {

    @ParameterizedTest
    @DisplayName("사용자가 회원인지 확인하는 테스트")
    @ValueSource(strings = {"OB", "YB"})
    void UserMemberTest(String userClassStr) {
        // given
        UserClass userClass = UserClass.valueOf(userClassStr);
        User user = User.builder()
                .userClass(userClass)
                .build();

        // when
        boolean member = user.isMember();

        // then
        assertThat(member).isTrue();
    }

    @Test
    @DisplayName("refreshToken이 없는 유저의 경우 에러가 발생하지 않는다.")
    void refreshTokenTest() {
        // given
        User user = User.builder().build();

        // when & then
        assertThatCode(() -> user.validateRefreshToken(new JwtService()))
                .doesNotThrowAnyException();
    }

}
