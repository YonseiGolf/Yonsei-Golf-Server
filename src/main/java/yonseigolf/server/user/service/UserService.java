package yonseigolf.server.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yonseigolf.server.user.dto.request.SignUpUserRequest;
import yonseigolf.server.user.dto.response.SessionUser;
import yonseigolf.server.user.entity.User;
import yonseigolf.server.user.repository.UserRepository;

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

        System.out.println("kakaoId = " + kakaoId);
        User user = repository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        System.out.println("user");
        return SessionUser.fromUser(user);
    }
}
