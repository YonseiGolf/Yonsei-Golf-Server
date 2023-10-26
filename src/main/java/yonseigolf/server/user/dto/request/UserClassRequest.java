package yonseigolf.server.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yonseigolf.server.user.entity.UserClass;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserClassRequest {

    private UserClass userClass;
}
