package yonseigolf.server.user.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
}
