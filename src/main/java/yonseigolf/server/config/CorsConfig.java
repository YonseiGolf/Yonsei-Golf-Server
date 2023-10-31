package yonseigolf.server.config;

import org.springframework.context.annotation.Configuration;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class CorsConfig implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (request.getHeader("Origin") != null) {
            if (request.getHeader("Origin").contains("yonseigolf.site")) {
                response.setHeader("Access-Control-Allow-Origin", "https://www.yonseigolf.site");

            }else if (request.getHeader("Origin").equals("www.yonseigolf.site")){
                response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
            } else {
                response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
            }
        }
        else {
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        }

        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods","GET, POST, PATCH, DELETE, HEAD, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "10");
        response.setHeader("Access-Control-Allow-Headers",
                "Origin, X-Requested-With, Content-Type, Accept, Authorization");

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}