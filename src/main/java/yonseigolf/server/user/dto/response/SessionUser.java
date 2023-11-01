package yonseigolf.server.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import yonseigolf.server.user.entity.User;
import yonseigolf.server.user.entity.UserRole;

import java.io.Serializable;

@Getter
@Builder
public class SessionUser implements Serializable {

    private long id;
    private String name;
    private boolean adminStatus;

    public static SessionUser fromUser(User user) {

        return SessionUser.builder()
                .id(user.getId())
                .name(user.getName())
                .adminStatus(user.getRole() != UserRole.MEMBER)
                .build();
    }
}
