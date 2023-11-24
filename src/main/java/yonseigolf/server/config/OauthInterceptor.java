package yonseigolf.server.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import yonseigolf.server.user.dto.response.LoggedInUser;
import yonseigolf.server.user.service.JwtService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class OauthInterceptor implements HandlerInterceptor {
    private final JwtService jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(request.getMethod().equals("OPTIONS")) {
            return true;
        }

        if (request.getHeader("Authorization") == null) {
            log.info("Authorization header is null");
            throw new IllegalArgumentException("Authorization header is null");
        }

        if (request.getHeader("Authorization").split(" ").length != 2) {
            log.info("Authorization header is not Bearer type");
            throw new IllegalArgumentException("Authorization header is not Bearer type");
        }

        String token = request.getHeader("Authorization").split(" ")[1];
        if (!request.getHeader("Authorization").split(" ")[0].equals("Bearer")) {
            throw new IllegalArgumentException("Authorization header is not Bearer type");
        }

        if (!jwtUtil.validateTokenIsExpired(token)) {
            throw new IllegalArgumentException("Token is expired");
        }

        if (!jwtUtil.validateTokenIsManipulated(token)) {
            throw new IllegalArgumentException("Token is manipulated");
        }

        LoggedInUser loggedInUser = jwtUtil.extractedUserInfoFromToken(token);
        request.setAttribute("kakaoId", loggedInUser.getId());

        return true;
    }
}
