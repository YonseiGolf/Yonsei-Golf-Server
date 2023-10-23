package yonseigolf.server.user.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

@Getter
@Builder
@JsonNaming(SnakeCaseStrategy.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class KakaoLoginResponse {

    private Long id;
    private KakaoAccount kakaoAccount;
}
