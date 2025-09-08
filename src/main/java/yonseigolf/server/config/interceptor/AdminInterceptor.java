package yonseigolf.server.config.interceptor;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import yonseigolf.server.user.dto.response.LoggedInUser;
import yonseigolf.server.user.entity.User;
import yonseigolf.server.user.repository.UserRepository;
import yonseigolf.server.user.service.JwtService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(request.getMethod().equals("OPTIONS")) {
            return true;
        }
        String token = request.getHeader("Authorization").split(" ")[1];
        LoggedInUser loggedInUser = jwtService.extractedUserFromToken(token, LoggedInUser.class);

        User user =userRepository.findById(loggedInUser.getId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        return user.isAdmin();
    }
}
