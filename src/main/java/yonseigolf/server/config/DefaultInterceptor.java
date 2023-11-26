package yonseigolf.server.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import yonseigolf.server.user.exception.AccessTokenExpiredException;
import yonseigolf.server.user.exception.NoAuthorizationException;
import yonseigolf.server.user.exception.NotBearerTypeException;
import yonseigolf.server.user.exception.TokenManipulatedException;
import yonseigolf.server.user.service.JwtService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultInterceptor implements HandlerInterceptor {

    private final JwtService jwtUtil;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        if(request.getMethod().equals("OPTIONS")) {
            return true;
        }

        if (request.getRequestURI().startsWith("/boards") && request.getMethod().equals("GET")) {
            return true;
        }

        if (request.getRequestURI().startsWith("/boards") && request.getMethod().equals("GET")) {
            return true;
        }

        if (request.getHeader("Authorization") == null) {
            log.info("Authorization header is null");
            throw new NoAuthorizationException("Authorization header is null");
        }

        if (request.getHeader("Authorization").split(" ").length != 2) {
            log.info("Authorization header is not Bearer type");
            throw new NotBearerTypeException("Authorization header is not Bearer type");
        }

        String token = request.getHeader("Authorization").split(" ")[1];
        if (!request.getHeader("Authorization").split(" ")[0].equals("Bearer")) {
            throw new NotBearerTypeException("Authorization header is not Bearer type");
        }

        if (!jwtUtil.validateTokenIsExpired(token)) {
            throw new AccessTokenExpiredException("Token is expired");
        }

        if (!jwtUtil.validateTokenIsManipulated(token)) {
            throw new TokenManipulatedException("Token is manipulated");
        }

        return true;
    }
}
