package yonseigolf.server.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import yonseigolf.server.user.dto.request.SignUpUserRequest;
import yonseigolf.server.user.dto.response.AdminResponse;
import yonseigolf.server.user.dto.response.SessionUser;
import yonseigolf.server.user.dto.response.SingleUserResponse;
import yonseigolf.server.user.entity.User;
import yonseigolf.server.user.entity.UserClass;
import yonseigolf.server.user.entity.UserRole;
import yonseigolf.server.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

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
        SessionUser sessionUser = userService.signUp(request, kaKaoId);
        long userId = sessionUser.getId();
        User user = userRepository.findById(userId).get();

        // then
        assertAll(
                () -> assertThat(user.getName()).isEqualTo(request.getName()),
                () -> assertThat(user.getPhoneNumber()).isEqualTo(request.getPhoneNumber()),
                () -> assertThat(user.getStudentId()).isEqualTo(request.getStudentId()),
                () -> assertThat(user.getMajor()).isEqualTo(request.getMajor()),
                () -> assertThat(user.getSemester()).isEqualTo(request.getSemester()),
                () -> assertThat(user.getKakaoId()).isEqualTo(kaKaoId),
                () -> assertThat(user.getRole()).isEqualTo(UserRole.MEMBER),
                () -> assertThat(user.getUserClass()).isEqualTo(UserClass.NONE),
                () -> assertThat(sessionUser.isAdminStatus()).isFalse()
        );
    }

    @Test
    @DisplayName("회원 가입이 되어 있다면 kakaoId로 로그인을 할 수 있다.")
    void signInTest() {
        // given
        User user = createUser(UserClass.OB, UserRole.MEMBER);

        User save = userRepository.save(user);

        // when
        SessionUser sessionUser = userService.signIn(save.getKakaoId());

        // then
        assertAll(
                () -> assertThat(sessionUser.getId()).isEqualTo(save.getId()),
                () -> assertThat(sessionUser.getName()).isEqualTo(save.getName()),
                () -> assertThat(sessionUser.isAdminStatus()).isFalse()
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
}
