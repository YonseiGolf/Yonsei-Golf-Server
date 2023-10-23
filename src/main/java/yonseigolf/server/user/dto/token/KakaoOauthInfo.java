package yonseigolf.server.user.dto.token;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Builder
@Component
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class KakaoOauthInfo {

    @Value("${KAKAO_CLIENT_ID}")
    private String clientId;
    @Value("${KAKAO_CLIENT_SECRET}")
    private String clientSecret;
    @Value("${KAKAO_REDIRECT_URI}")
    private String redirectUri;
    @Value("${KAKAO_LOGIN_URI}")
    private String loginUri;
}
