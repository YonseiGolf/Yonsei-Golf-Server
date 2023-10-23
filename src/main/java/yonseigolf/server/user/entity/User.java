package yonseigolf.server.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yonseigolf.server.user.dto.request.SignUpUserRequest;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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
    private int age;
    private String name;
    private String phoneNumber;
    private int studentId;
    private String major;
    private int semester;

    public static User of(SignUpUserRequest request, Long kakaoId) {

            return User.builder()
                    .kakaoId(kakaoId)
                    .age(request.getAge())
                    .name(request.getName())
                    .phoneNumber(request.getPhoneNumber())
                    .studentId(request.getStudentId())
                    .major(request.getMajor())
                    .semester(request.getSemester())
                    .build();
    }
}
