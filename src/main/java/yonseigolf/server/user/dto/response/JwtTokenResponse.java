package yonseigolf.server.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JwtTokenResponse {

    private String accessToken;
}
