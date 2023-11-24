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
import yonseigolf.server.user.dto.response.*;
import yonseigolf.server.user.dto.token.KakaoOauthInfo;
import yonseigolf.server.user.dto.token.OauthToken;
import yonseigolf.server.user.entity.UserClass;
import yonseigolf.server.user.service.JwtService;
import yonseigolf.server.user.service.OauthLoginService;
import yonseigolf.server.user.service.UserService;
import yonseigolf.server.util.CustomResponse;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;

@Slf4j
@Controller
public class UserController {

    private static final String SESSION_KAKAO_USER = "kakaoUser";
    private final UserService userService;
    private final OauthLoginService oauthLoginService;
    private final KakaoOauthInfo kakaoOauthInfo;
    private final JwtService jwtUtil;

    @Autowired
    public UserController(UserService userService, OauthLoginService oauthLoginService, KakaoOauthInfo kakaoOauthInfo, JwtService jwtUtil) {

        this.userService = userService;
        this.oauthLoginService = oauthLoginService;
        this.kakaoOauthInfo = kakaoOauthInfo;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/oauth/kakao")
    public ResponseEntity<CustomResponse<JwtTokenResponse>> kakaoLogin(@RequestBody KakaoCode kakaoCode) {
        OauthToken oauthToken = oauthLoginService.getOauthToken(kakaoCode.getKakaoCode(), kakaoOauthInfo);
        KakaoLoginResponse kakaoLoginResponse = oauthLoginService.processKakaoLogin(oauthToken.getAccessToken(), kakaoOauthInfo.getLoginUri());

        String token = jwtUtil.createToken(
                JwtTokenUser.builder()
                        .id(kakaoLoginResponse.getId())
                        .build(),
                new Date(new Date().getTime() + 360000)
        );

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse(
                        "카카오 로그인 성공",
                        JwtTokenResponse.builder().accessToken(token).build())
                );
    }

    // TODO: 세션이 아닌 JWT Token으로부터 userId 가져와야 함, user의 refresh token이 없거나 만료된 경우 재발급
    @PostMapping("/users/signIn")
    public ResponseEntity<CustomResponse<JwtTokenResponse>> signIn(@RequestAttribute(required = false) Long kakaoId, HttpServletResponse response) {

        LoggedInUser loggedInUser = userService.signIn(kakaoId);

        // 30분 시간 제한
        Date date = new Date(new Date().getTime() + 1800000);
        String tokenReponse = jwtUtil.createLoggedInUserToken(loggedInUser, date);

        // refresh token 검증후 발급
        // 만료 기한은 2주일
        if (!userService.validateRefreshToken(kakaoId, jwtUtil)) {
            System.out.println("refresh token 발급");
            Date expireDate = new Date(new Date().getTime() + 1209600000);
            String refreshToken = jwtUtil.createRefreshToken(loggedInUser.getId(), expireDate);
            userService.saveRefreshToken(loggedInUser.getId(), refreshToken);
            createRefreshToken(response, refreshToken);
        }

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("로그인 성공",
                        JwtTokenResponse.builder()
                                .accessToken(tokenReponse)
                                .build())
                );
    }

    private void createRefreshToken(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true); // HTTP Only 설정
        cookie.setSecure(true); // Secure 설정, TODO: 배포할 땐 true로 변경
        cookie.setPath("/"); // 경로 설정
        cookie.setMaxAge(60 * 60 * 24 * 14); // 2주일
        response.addCookie(cookie); // 응답에 쿠키 추가
    }

    // TODO: refreshToken 무효화 시키고, 클라이언트에서 access token 폐기
    @PostMapping("/users/logout")
    public ResponseEntity<CustomResponse<Void>> logOut(HttpSession session) {

        session.removeAttribute("user");

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("로그아웃 성공"));
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

        LoggedInUser sessionUser = userService.signUp(request, kakaoId);

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
