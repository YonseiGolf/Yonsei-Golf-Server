package yonseigolf.server.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import yonseigolf.server.user.dto.request.SignUpUserRequest;
import yonseigolf.server.user.dto.response.AdminResponse;
import yonseigolf.server.user.dto.response.LoggedInUser;
import yonseigolf.server.user.dto.response.SingleUserResponse;
import yonseigolf.server.user.entity.*;
import yonseigolf.server.user.repository.RefreshTokenRepository;
import yonseigolf.server.user.repository.UserRepository;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("사용자는 회원가입을 할 수 있다.")
    void signUpTest() {
        // given
        SignUpUserRequest request = SignUpUserRequest.builder()
                .name("이름")
                .phoneNumber("010-1234-5678")
                .studentId(1)
                .major("컴퓨터과학과")
                .semester(1)
                .build();
        Long kaKaoId = 1L;

        // when
        LoggedInUser sessionUser = userService.signUp(request, kaKaoId);
        long userId = sessionUser.getId();
        User user = userRepository.findById(userId).get();

        // then
        assertAll(
//                () -> assertThat(user.getPhoneNumber()).isEqualTo(request.getPhoneNumber()),
//                () -> assertThat(user.getRole()).isEqualTo(UserRole.MEMBER),
//                () -> assertThat(sessionUser.isAdminStatus()).isFalse(),
                () -> assertThat(user.getName()).isEqualTo(request.getName()),
                () -> assertThat(user.getStudentId()).isEqualTo(request.getStudentId()),
                () -> assertThat(user.getMajor()).isEqualTo(request.getMajor()),
                () -> assertThat(user.getSemester()).isEqualTo(request.getSemester()),
                () -> assertThat(user.getKakaoId()).isEqualTo(kaKaoId),
                () -> assertThat(user.getUserClass()).isEqualTo(UserClass.NONE)
        );
    }

    @ParameterizedTest
    @DisplayName("회원 가입이 되어 있다면 kakaoId로 로그인을 할 수 있다.")
    @EnumSource(UserRole.class)
    void signInTest(UserRole  userRole) {
        // given

        User user = createUser(UserClass.OB, userRole);

        User save = userRepository.save(user);

        // when
        LoggedInUser sessionUser = userService.signIn(save.getKakaoId());

        // then
        assertAll(
                () -> assertThat(sessionUser.getId()).isEqualTo(save.getId()),
                () -> assertThat(sessionUser.getName()).isEqualTo(save.getName())
        );
    }

    @Test
    @DisplayName("회장 1명과 부회장 2명을 조회할 수 있다.")
    void getLeadersTest() {
        // given
        User leader = createUser(UserClass.OB, UserRole.LEADER);
        User firstAssistantLeader = createUser(UserClass.OB, UserRole.ASSISTANT_LEADER);
        User secondAssistantLeader = createUser(UserClass.OB, UserRole.ASSISTANT_LEADER);

        userRepository.save(leader);
        userRepository.save(firstAssistantLeader);
        userRepository.save(secondAssistantLeader);

        // when
        AdminResponse leaders = userService.getLeaders();

        // then
        assertAll(
                () -> assertThat(leaders.getLeader().getName()).isEqualTo(leader.getName()),
                () -> assertThat(leaders.getAssistantLeaders().get(0).getName()).isEqualTo(firstAssistantLeader.getName()),
                () -> assertThat(leaders.getAssistantLeaders().get(1).getName()).isEqualTo(secondAssistantLeader.getName())
        );
    }

    @Test
    @DisplayName("사용자는 OB, YB 별로 회원을 조회할 수 있다.")
    void findUsersByClassTest() {
        // given
        User user = createUser(UserClass.OB, UserRole.MEMBER);
        User secondUser = createUser(UserClass.OB, UserRole.MEMBER);

        userRepository.save(user);
        userRepository.save(secondUser);

        // when
        Page<SingleUserResponse> allUsersByClass = userService.findUsersByClass(PageRequest.of(0, Integer.MAX_VALUE), UserClass.OB);

        // then
        assertAll(
                () -> assertThat(allUsersByClass.getTotalElements()).isEqualTo(2),
                () -> assertThat(allUsersByClass.getContent().get(0).getUserClass()).isEqualTo(UserClass.OB),
                () -> assertThat(allUsersByClass.getContent().get(1).getUserClass()).isEqualTo(UserClass.OB)
        );
    }

    @Test
    @DisplayName("회원의 OB, YB 상태를 변경할 수 있다.")
    void updateUserClassTest() {
        // given
        User user = createUser(UserClass.OB, UserRole.MEMBER);
        User savedUser = userRepository.save(user);
        UserClass updatedUserClass = UserClass.YB;

        // when
        userService.updateUserClass(savedUser.getId(), updatedUserClass);
        User updatedUser = userRepository.findById(savedUser.getId()).get();

        // then
        assertThat(updatedUser.getUserClass()).isEqualTo(updatedUserClass);
    }

    private User createUser(UserClass userClass, UserRole userRole) {

        return User.builder()
                .name("이름")
                .phoneNumber("010-1234-5678")
                .studentId(1)
                .major("컴퓨터과학과")
                .semester(1)
                .kakaoId(1L)
                .role(userRole)
                .userClass(userClass)
                .build();
    }

    @Test
    @DisplayName("로그인을 하는데 카카오 아이디가 없다면 예외가 발생한다.")
    void noKakaoIdExceptionTest() {
        // given
        Long notExistKakaoId = 100L;

        // when & then
        assertThatThrownBy(() -> userService.signIn(notExistKakaoId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 유저입니다.");
    }

    @Test
    @DisplayName("회장을 지정해주지 않은 경우 예외가 발생한다.")
    void noLeaderException() {

        // when & then
        assertThatThrownBy(() -> userService.getLeaders())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("회장이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("없는 유저에게 메서드로 유저 클래스를 변경하려고 하면 예외가 발생한다.")
    void notExistingUserExceptionTest() {
        // given
        Long notExistingUserId = 100L;

        // then
        assertThatThrownBy(() -> userService.updateUserClass(notExistingUserId, UserClass.OB))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 유저입니다.");
    }

    @Test
    @DisplayName("refresh token에 이상이 없는 경우 예외가 발생하지 않는다.")
    void noThrowRefreshTokenTest() {
        // given
        User user = User.builder()
                .kakaoId(1L)
                .name("이름")
                .phoneNumber("010-1234-5678")
                .studentId(1)
                .major("컴퓨터과학과")
                .semester(1)
                .role(UserRole.MEMBER)
                .userClass(UserClass.OB)
                .build();
        User savedUser = userRepository.save(user);

        String refreshToken = jwtService.createRefreshToken(savedUser.getId(), new Date(new Date().getTime() + 1800000));
        userService.saveRefreshToken(savedUser.getId(),refreshToken);

        // when & then
        userService.validateRefreshToken(savedUser.getId(), jwtService);
    }

    @Test
    @DisplayName("refresh token validate test")
    void validateRefreshTokenTest() {
        // given
        RefreshToken refreshToken = RefreshToken
                .builder()
                .refreshToken("refreshToken")
                .build();
        RefreshToken savedRefreshToken = refreshTokenRepository.save(refreshToken);

        User user = User.builder()
                .kakaoId(1L)
                .name("이름")
                .phoneNumber("010-1234-5678")
                .studentId(1)
                .major("컴퓨터과학과")
                .semester(1)
                .role(UserRole.MEMBER)
                .userClass(UserClass.OB)
                .refreshToken(savedRefreshToken)
                .build();
        User savedUser = userRepository.save(user);

        // when
        userService.invalidateRefreshToken(savedUser.getId());
        User findUser = userRepository.findById(savedUser.getId()).orElseGet(() -> User.builder().build());

        // then
        assertThat(findUser.getRefreshToken()).isNull();
    }

    @Test
    @DisplayName("refresh token을 통해 access token을 재생성 할 수 있다.")
    void refreshAccessTokenTest() {
        // given
        User user = User.builder()
                .kakaoId(1L)
                .name("이름")
                .phoneNumber("010-1234-5678")
                .studentId(1)
                .major("컴퓨터과학과")
                .semester(1)
                .role(UserRole.MEMBER)
                .userClass(UserClass.OB)
                .build();
        User savedUser = userRepository.save(user);
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsb2dpbl9tZW1iZXIiLCJ1c2VyUHJvZmlsZSI6eyJpZCI6MzUsIm5hbWUiOiLsnoTrj5ntmIQiLCJhZG1pblN0YXR1cyI6dHJ1ZSwibWVtYmVyU3RhdHVzIjp0cnVlfSwiZXhwIjoxNzAwOTc5MjU5fQ.MegKm0Oj7wYcKMtanyLqt0x5L4X7VC_tfBwJvfGSG1c";

        userService.saveRefreshToken(savedUser.getId(), token);

        // when
        String accessToken = userService.generateAccessToken(savedUser.getId(), jwtService, new Date(new Date().getTime() + 1800000));

        // then
        assertThat(accessToken).isNotNull();
    }
}
