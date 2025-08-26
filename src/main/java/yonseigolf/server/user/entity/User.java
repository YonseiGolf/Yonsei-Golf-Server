package yonseigolf.server.user.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yonseigolf.server.user.dto.request.SignUpUserRequest;

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

    public static User createUserForForeignKey(Long id) {

        return User.builder()
                .id(id)
                .build();
    }

    public void updateUserClass(UserClass userClass) {

        this.userClass = userClass;
    }

    public boolean isAdmin() {

        return this.role == UserRole.LEADER ||
                this.role == UserRole.ASSISTANT_LEADER ||
                this.role == UserRole.OB_LEADER ||
                this.role == UserRole.OB_ASSISTANT_LEADER;
    }

    public boolean isMember() {

            return this.userClass == UserClass.YB ||
                    this.userClass == UserClass.OB;
    }

    public boolean checkOwner(Long userId) {
        return this.id == userId;
    }

    public void updateRegisteredUser(
        String phoneNumber,
        String major,
        int semester,
        long kakaoId
    ) {
        this.phoneNumber = phoneNumber;
        this.major = major;
        this.semester = semester;
        this.kakaoId = kakaoId;
    }
}
