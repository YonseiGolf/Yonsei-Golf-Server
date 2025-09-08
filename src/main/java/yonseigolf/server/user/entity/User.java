package yonseigolf.server.user.entity;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yonseigolf.server.user.dto.request.SignUpUserRequest;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    private User(
        long kakaoId,
        String name,
        String phoneNumber,
        int studentId,
        String major,
        int semester
    ) {
        this.kakaoId = kakaoId;
        this.name = Objects.requireNonNull(name);
        this.phoneNumber = Objects.requireNonNull(phoneNumber);
        this.studentId = studentId;
        this.major = Objects.requireNonNull(major);
        this.semester = semester;
        this.role = UserRole.MEMBER;
        this.userClass = UserClass.NONE;
    }

    public static User create(
        long kakaoId,
        String name,
        String phoneNumber,
        int studentId,
        String major,
        int semester
    ) {
        return new User(
            kakaoId,
            name,
            phoneNumber,
            studentId,
            major,
            semester
        );
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

        return this.role.isAdmin();
    }

    public boolean isMember() {

            return this.userClass.isMember();
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
