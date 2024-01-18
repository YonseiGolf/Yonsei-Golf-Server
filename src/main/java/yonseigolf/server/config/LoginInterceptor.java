package yonseigolf.server.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import yonseigolf.server.user.dto.response.LoggedInUser;
import yonseigolf.server.user.exception.DuplicatedLoginException;
import yonseigolf.server.user.service.JwtService;
import yonseigolf.server.user.service.PreventDuplicateLoginService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtService jwtUtil;
    private final PreventDuplicateLoginService preventDuplicateLoginService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        if(request.getMethod().equals("OPTIONS")) {
            return true;
        }
        if (request.getRequestURI().startsWith("/boards") && request.getMethod().equals("GET")) {
            return true;
        }
        String token = request.getHeader("Authorization").split(" ")[1];
        LoggedInUser loggedInUser = jwtUtil.extractedUserFromToken(token, LoggedInUser.class);

        boolean duplicated = preventDuplicateLoginService.checkDuplicatedLogin(loggedInUser.getId(), token);

        if (!duplicated) {

            invalidateRefreshToken(response);
            throw new DuplicatedLoginException("중복 로그인이 발생했습니다.");
        }

        request.setAttribute("userId", loggedInUser.getId());

        return true;
    }

    private void invalidateRefreshToken(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null); // 쿠키 이름을 Refresh Token 쿠키 이름과 동일하게 설정
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // 프로덕션 환경에서는 true로 설정
        cookie.setPath("/"); // Refresh Token 쿠키와 동일한 경로 설정
        cookie.setMaxAge(0); // 쿠키의 만료 시간을 0으로 설정하여 즉시 만료
        response.addCookie(cookie);
    }
}
