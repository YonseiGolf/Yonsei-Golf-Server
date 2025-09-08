package yonseigolf.server.config.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import yonseigolf.server.user.entity.User;
import yonseigolf.server.user.entity.UserClass;
import yonseigolf.server.user.service.UserService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlackListInterceptor implements HandlerInterceptor {
    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) throws Exception {

        if(request.getMethod().equals("OPTIONS")) {
            return true;
        }

        long userId = (long) request.getAttribute("userId");
        User user = userService.findById(userId);

        if (user.getUserClass() == UserClass.BLACK_LIST) {
            throw new IllegalArgumentException("제명된 회원은 접근할 수 없습니다.");
        }

        return true;
    }
}
