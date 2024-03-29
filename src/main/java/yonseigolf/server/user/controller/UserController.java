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
import yonseigolf.server.user.exception.RefreshTokenException;
import yonseigolf.server.user.service.JwtService;
import yonseigolf.server.user.service.OauthLoginService;
import yonseigolf.server.user.service.UserService;
import yonseigolf.server.util.CustomResponse;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Slf4j
@Controller
public class UserController {

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
    public ResponseEntity<CustomResponse<JwtTokenResponse>> kakaoLogin(@RequestBody KakaoCode kakaoCode, HttpServletResponse response) {
        OauthToken oauthToken = oauthLoginService.getOauthToken(kakaoCode.getKakaoCode(), kakaoOauthInfo);
        KakaoLoginResponse kakaoLoginResponse = oauthLoginService.processKakaoLogin(oauthToken.getAccessToken(), kakaoOauthInfo.getLoginUri());

        String token = jwtUtil.createToken(
                JwtTokenUser.builder()
                        .id(kakaoLoginResponse.getId())
                        .build(),
                new Date(new Date().getTime() + 360000)
        );

        createRefreshToken(response, oauthToken.getRefreshToken());

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse(
                        "카카오 로그인 성공",
                        JwtTokenResponse.builder().accessToken(token).build())
                );
    }

    @PostMapping("/users/signIn")
    public ResponseEntity<CustomResponse<JwtTokenResponse>> signIn(@RequestAttribute(required = false) Long kakaoId, HttpServletResponse response) {

        LoggedInUser loggedInUser = userService.signIn(kakaoId);

        Date date = new Date(new Date().getTime() + 3600000);
        String tokenReponse = jwtUtil.createToken(loggedInUser, date);

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
        cookie.setSecure(true); // Secure 설정,
        cookie.setPath("/"); // 경로 설정
        cookie.setMaxAge(60 * 60 * 24 * 14); // 2주일
        response.addCookie(cookie); // 응답에 쿠키 추가
    }

    @PostMapping("/users/loggedIn")
    public ResponseEntity<CustomResponse<Void>> loggedIn() {

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("로그인 상태입니다."));
    }

    @PostMapping("/users/signIn/refresh")
    public ResponseEntity<CustomResponse<JwtTokenResponse>> refreshAccessToken(HttpServletRequest request) {

        String refreshToken = findRefreshToken(request);
        if (refreshToken == null) {
            throw new RefreshTokenException("Refresh Token이 존재하지 않습니다.");
        }

        long kakaoId = oauthLoginService.refreshAccessToken(refreshToken, kakaoOauthInfo);
        LoggedInUser loggedInUser = userService.signIn(kakaoId);

        String jwt = jwtUtil.createToken(loggedInUser, new Date(new Date().getTime() + 360000));

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse(
                        "토큰 재발급 성공",
                        JwtTokenResponse.builder()
                                .accessToken(jwt)
                                .build()
                ));
    }

    private String findRefreshToken(HttpServletRequest request) {
        String refreshToken = null;

        // 쿠키에서 Refresh Token 추출
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        return refreshToken;
    }

    @PostMapping("/users/logout")
    public ResponseEntity<CustomResponse<Void>> logOut(@RequestAttribute Long userId, HttpServletResponse response) {

        // Cookie 삭제
        invalidateCookie(response);

        return ResponseEntity
                .ok()
                .body(CustomResponse.successResponse("로그아웃 성공"));
    }

    private void invalidateCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null); // 쿠키 이름을 Refresh Token 쿠키 이름과 동일하게 설정
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // 프로덕션 환경에서는 true로 설정
        cookie.setPath("/"); // Refresh Token 쿠키와 동일한 경로 설정
        cookie.setMaxAge(0); // 쿠키의 만료 시간을 0으로 설정하여 즉시 만료
        response.addCookie(cookie);
    }

    @PostMapping("/users/signUp")
    public ResponseEntity<CustomResponse<Void>> signUp(@RequestBody @Validated SignUpUserRequest request, @RequestAttribute Long kakaoId) {

        if (kakaoId == null) {
            throw new IllegalArgumentException("[ERROR] 카카오 로그인을 먼저 해주세요.");
        }

        userService.signUp(request, kakaoId);

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
