package yonseigolf.server.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yonseigolf.server.user.entity.User;
import yonseigolf.server.user.entity.UserRole;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String name;
    private String phoneNumber;
    private UserRole role;

    public static UserResponse fromUser(User user) {

        return UserResponse.builder()
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .build();
    }
}
