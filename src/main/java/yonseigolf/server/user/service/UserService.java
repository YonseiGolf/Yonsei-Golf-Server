package yonseigolf.server.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yonseigolf.server.user.dto.request.SignUpUserRequest;
import yonseigolf.server.user.dto.response.*;
import yonseigolf.server.user.entity.User;
import yonseigolf.server.user.entity.UserClass;
import yonseigolf.server.user.entity.UserRole;
import yonseigolf.server.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository repository;

    @Autowired
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public LoggedInUser signUp(SignUpUserRequest request, Long kakaoId) {

        User savedUser = repository.save(User.of(request, kakaoId));

        return LoggedInUser.fromUser(savedUser);
    }

    public LoggedInUser signIn(Long kakaoId) {

        User user = findByKakaoId(kakaoId);
        return LoggedInUser.fromUser(user);
    }

    public AdminResponse getLeaders() {

        User leader = repository.findLeaderByRole(UserRole.LEADER)
                .orElseThrow(() -> new IllegalArgumentException("회장이 존재하지 않습니다."));

        List<UserResponse> assistantLeaders = repository.findAssistantLeadersByRole(UserRole.ASSISTANT_LEADER).stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());

        return AdminResponse.of(UserResponse.fromUser(leader), assistantLeaders);
    }

    public Page<SingleUserResponse> findUsersByClass(Pageable pageable, UserClass userClass) {

        return repository.findAllUsers(pageable, userClass);
    }

    @Transactional
    public void updateUserClass(Long userId, UserClass userClass) {

        User user = findById(userId);
        user.updateUserClass(userClass);
    }

    private User findById(Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }

    private User findByKakaoId(Long kakaoId) {

        return repository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }
}
