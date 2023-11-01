package yonseigolf.server.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import yonseigolf.server.user.dto.request.KakaoCode;
import yonseigolf.server.user.dto.request.SignUpUserRequest;
import yonseigolf.server.user.dto.request.UserClassRequest;
import yonseigolf.server.user.dto.response.AdminResponse;
import yonseigolf.server.user.dto.response.KakaoLoginResponse;
import yonseigolf.server.user.dto.response.SessionUser;
import yonseigolf.server.user.dto.response.SingleUserResponse;
import yonseigolf.server.user.dto.token.KakaoOauthInfo;
import yonseigolf.server.user.dto.token.OauthToken;
import yonseigolf.server.user.entity.UserClass;
import yonseigolf.server.user.service.OauthLoginService;
import yonseigolf.server.user.service.UserService;
import yonseigolf.server.util.CustomResponse;

import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class UserController {

    private static final String SESSION_KAKAO_USER = "kakaoUser";
    private final UserService userService;
    private final OauthLoginService oauthLoginService;
    private final KakaoOauthInfo kakaoOauthInfo;

    @Autowired
    public UserController(UserService userService, OauthLoginService oauthLoginService, KakaoOauthInfo kakaoOauthInfo) {

        this.userService = userService;
        this.oauthLoginService = oauthLoginService;
        this.kakaoOauthInfo = kakaoOauthInfo;
    }

    @PostMapping("/oauth/kakao")
    public ResponseEntity<CustomResponse<Void>> kakaoLogin(@RequestBody KakaoCode kakaoCode, HttpSession session) {

        OauthToken oauthToken = oauthLoginService.getOauthToken(kakaoCode.getValue(), kakaoOauthInfo);
        KakaoLoginResponse kakaoLoginResponse = oauthLoginService.processKakaoLogin(oauthToken.getAccessToken(), kakaoOauthInfo.getLoginUri());

        session.setAttribute(SESSION_KAKAO_USER, kakaoLoginResponse.getId());

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("카카오 로그인 성공"));
    }

    @PostMapping("/users/signIn")
    public ResponseEntity<CustomResponse<SessionUser>> signIn(HttpSession session) {
        Long id = (Long) session.getAttribute(SESSION_KAKAO_USER);

        SessionUser sessionUser = userService.signIn(id);
        session.setAttribute("user", sessionUser);

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("로그인 성공", sessionUser));
    }

    @PostMapping("/users/signUp")
    public ResponseEntity<CustomResponse<Void>> signUp(@RequestBody @Validated SignUpUserRequest request, HttpSession session) {

        Long kakaoId = (Long) session.getAttribute(SESSION_KAKAO_USER);

        if (session.getAttribute("user") != null) {
            throw new IllegalArgumentException("[ERROR] 이미 로그인된 상태입니다.");
        }
        if (kakaoId == null) {
            throw new IllegalArgumentException("[ERROR] 카카오 로그인을 먼저 해주세요.");
        }

        SessionUser sessionUser = userService.signUp(request, kakaoId);

        session.removeAttribute(SESSION_KAKAO_USER);
        session.setAttribute("user", sessionUser);

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("회원가입 성공"));
    }

    @GetMapping("/admin/users")
    public ResponseEntity<CustomResponse<Page<SingleUserResponse>>> findAllUsers(Pageable pageable, UserClass userClass) {

        Page<SingleUserResponse> users = userService.findUsersByClass(pageable, userClass);

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("유저 조회 성공", users));
    }

    @PatchMapping("/admin/users/{userId}")
    public ResponseEntity<CustomResponse<Void>> updateUserClass(@PathVariable Long userId, @RequestBody UserClassRequest userClass) {

        userService.updateUserClass(userId, userClass.getUserClass());

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("유저 정보 변경 성공"));
    }

    @GetMapping("/users/leaders")
    public ResponseEntity<CustomResponse<AdminResponse>> getLeaders() {

        AdminResponse leaders = userService.getLeaders();


        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("리더 조회 성공", leaders));
    }

    @GetMapping("/healthcheck")
    public ResponseEntity<CustomResponse<Void>> healthCheck() {

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("서버 정상 작동 중"));
    }
}
