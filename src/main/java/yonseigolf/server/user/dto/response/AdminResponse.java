package yonseigolf.server.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminResponse {

    private UserResponse leader;
    private List<UserResponse> assistantLeaders;

    public static AdminResponse of(UserResponse leader, List<UserResponse> assistantLeaders) {

        return AdminResponse.builder()
                .leader(leader)
                .assistantLeaders(assistantLeaders)
                .build();
    }
}
