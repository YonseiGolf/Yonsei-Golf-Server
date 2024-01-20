package yonseigolf.server.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yonseigolf.server.user.dto.request.SignUpUserRequest;
import yonseigolf.server.user.dto.response.AdminResponse;
import yonseigolf.server.user.dto.response.LoggedInUser;
import yonseigolf.server.user.dto.response.SingleUserResponse;
import yonseigolf.server.user.dto.response.UserResponse;
import yonseigolf.server.user.entity.User;
import yonseigolf.server.user.entity.UserClass;
import yonseigolf.server.user.entity.UserRole;
import yonseigolf.server.user.repository.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    public LoggedInUser signUp(SignUpUserRequest request, Long kakaoId) {

        User savedUser = userRepository.save(User.of(request, kakaoId));

        return LoggedInUser.fromUser(savedUser);
    }

    public LoggedInUser signIn(Long kakaoId) {

        User user = findByKakaoId(kakaoId);
        return LoggedInUser.fromUser(user);
    }

    public AdminResponse getLeaders() {

        User leader = userRepository.findLeaderByRole(UserRole.LEADER)
                .orElseThrow(() -> new IllegalArgumentException("회장이 존재하지 않습니다."));

        List<UserResponse> assistantLeaders = userRepository.findAssistantLeadersByRole(UserRole.ASSISTANT_LEADER).stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());

        return AdminResponse.of(UserResponse.fromUser(leader), assistantLeaders);
    }

    public Page<SingleUserResponse> findUsersByClass(Pageable pageable, UserClass userClass) {

        return userRepository.findAllUsers(pageable, userClass);
    }

    @Transactional
    public void updateUserClass(Long userId, UserClass userClass) {

        User user = findById(userId);
        user.updateUserClass(userClass);
    }

    public String generateAccessToken(Long userId, JwtService jwtService, Date expiredAt) {
        User user = findById(userId);
        LoggedInUser loggedInUser = LoggedInUser.fromUser(user);

        return jwtService.createToken(loggedInUser, expiredAt);
    }

    private User findById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }

    public User findByKakaoId(Long kakaoId) {

        return userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }
}
