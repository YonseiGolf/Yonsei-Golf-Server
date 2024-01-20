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
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtService jwtUtil;

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

        request.setAttribute("userId", loggedInUser.getId());

        return true;
    }
}
