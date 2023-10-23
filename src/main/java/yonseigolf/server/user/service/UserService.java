package yonseigolf.server.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yonseigolf.server.user.dto.request.SignUpUserRequest;
import yonseigolf.server.user.dto.response.AdminResponse;
import yonseigolf.server.user.dto.response.SessionUser;
import yonseigolf.server.user.dto.response.UserResponse;
import yonseigolf.server.user.entity.User;
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

    public SessionUser signUp(SignUpUserRequest request, Long kakaoId) {

        User savedUser = repository.save(User.of(request, kakaoId));

        return SessionUser.fromUser(savedUser);
    }

    public SessionUser signIn(Long kakaoId) {

        User user = findByKakaoId(kakaoId);
        return SessionUser.fromUser(user);
    }

    public AdminResponse getLeaders() {

        User leader = repository.findLeaderByRole(UserRole.LEADER)
                .orElseThrow(() -> new IllegalArgumentException("회장이 존재하지 않습니다."));

        List<UserResponse> assistantLeaders = repository.findAssistantLeadersByRole(UserRole.ASSISTANT_LEADER).stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());

        return AdminResponse.of(UserResponse.fromUser(leader), assistantLeaders);
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
