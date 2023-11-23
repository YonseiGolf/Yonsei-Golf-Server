package yonseigolf.server.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yonseigolf.server.user.entity.User;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoggedInUser implements Serializable {

    private long id;
    private String name;
    private boolean adminStatus;
    private boolean memberStatus;

    public static LoggedInUser fromUser(User user) {

        return LoggedInUser.builder()
                .id(user.getId())
                .name(user.getName())
                .adminStatus(user.isAdmin())
                .memberStatus(user.isMember())
                .build();
    }
}
