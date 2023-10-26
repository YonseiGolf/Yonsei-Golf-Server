package yonseigolf.server.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yonseigolf.server.user.dto.request.SignUpUserRequest;

import javax.persistence.*;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long kakaoId;
    private String name;
    private String phoneNumber;
    private int studentId;
    private String major;
    private int semester;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    @Enumerated(EnumType.STRING)
    private UserClass userClass;

    public static User of(SignUpUserRequest request, Long kakaoId) {

        return User.builder()
                .kakaoId(kakaoId)
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .studentId(request.getStudentId())
                .major(request.getMajor())
                .semester(request.getSemester())
                .role(UserRole.MEMBER)
                .userClass(UserClass.NONE)
                .build();
    }
}
