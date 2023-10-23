package yonseigolf.server.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpUserRequest {

    private int age;
    private String name;
    private String phoneNumber;
    private int studentId;
    private String major;
    private int semester;
}
