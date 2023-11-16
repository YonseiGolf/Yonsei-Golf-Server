package yonseigolf.server.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yonseigolf.server.user.entity.User;
import yonseigolf.server.user.entity.UserClass;
import yonseigolf.server.user.entity.UserRole;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingleUserResponse {

    private long id;
    private long kakaoId;
    private String name;
    private String phoneNumber;
    private long studentId;
    private String major;
    private long semester;
    private UserRole role;
    private UserClass userClass;

    // TODO: 전화번호 변경할 것
    public static SingleUserResponse fromUser(User user) {

        return SingleUserResponse.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .name(user.getName())
                .phoneNumber("QA 비활성화중")
                .studentId(user.getStudentId())
                .major(user.getMajor())
                .semester(user.getSemester())
                .role(user.getRole())
                .userClass(user.getUserClass())
                .build();
    }
}
