package yonseigolf.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Profile("!test")
@Configuration
public class CorsConfig implements Filter {

    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final String ORIGIN = "Origin";
    private static final String LOCAL_CLIENT = "http://localhost:3000";
    private static final String PROD_CLIENT_WWW = "https://www.yonseigolf.site";
    private static final String PROD_CLIENT = "https://yonseigolf.site";
    private static final String DELIMITER = ", ";

    @Override
    public void init(FilterConfig filterConfig) {
        // 필요한 초기화 작업이 없습니다.
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (request.getHeader(ORIGIN) != null) {

            if (request.getHeader(ORIGIN).equals(PROD_CLIENT)) {

                response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, PROD_CLIENT);
            } else if (request.getHeader(ORIGIN).equals(PROD_CLIENT_WWW)) {

                response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, PROD_CLIENT_WWW);
            } else {

                response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, LOCAL_CLIENT);
            }
        } else {
            response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, LOCAL_CLIENT);
        }

        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods",
                HttpMethod.GET.name() + DELIMITER +
                        HttpMethod.POST.name() + DELIMITER +
                        HttpMethod.PATCH.name() + DELIMITER +
                        HttpMethod.DELETE.name() + DELIMITER +
                        HttpMethod.HEAD.name() + DELIMITER +
                        HttpMethod.OPTIONS.name());
        response.setHeader("Access-Control-Max-Age", "10");
        response.setHeader("Access-Control-Allow-Headers",
                "Origin, X-Requested-With, Content-Type, Accept, Authorization");

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // 필요한 리소스 정리 작업이 없습니다.
    }
}