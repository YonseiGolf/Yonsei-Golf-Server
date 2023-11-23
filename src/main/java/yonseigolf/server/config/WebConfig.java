package yonseigolf.server.config;

import com.amazonaws.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final OauthInterceptor oauthInterceptor;
    private final LoginInterceptor loginInterceptor;


    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**") // 모든 경로에 대해
                .allowedOrigins(
                        "http://localhost:3000",
                        "https://www.yonseigolf.site",
                        "https://yonseigolf.site"
                )
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "HEAD", "OPTIONS")
                .allowedHeaders("Origin", "X-Requested-With", "Content-Type", "Accept", "Authorization")
                .allowCredentials(true);
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(oauthInterceptor)
                        .addPathPatterns("/users/signIn");

        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/boards/**")
                .excludePathPatterns("/oauth/kakao");
    }
}
