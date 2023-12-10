package yonseigolf.server.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yonseigolf.server.user.dto.request.SignUpUserRequest;
import yonseigolf.server.user.exception.RefreshTokenExpiredException;
import yonseigolf.server.user.service.JwtService;

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
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refresh_token_id")
    private RefreshToken refreshToken;

    // TODO: 회원가입 시 멤버로 설정할 것
    public static User of(SignUpUserRequest request, Long kakaoId) {

        return User.builder()
                .kakaoId(kakaoId)
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .studentId(request.getStudentId())
                .major(request.getMajor())
                .semester(request.getSemester())
                .role(UserRole.OB_ASSISTANT_LEADER)
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

    public void validateRefreshToken(JwtService jwtUtil) {
        // refresh token이 없을 경우 발급한다.
        if (this.refreshToken == null) {
            return;
        }
        // refresh token이 만료된 경우 재발급한다.
        this.refreshToken.isBeforeExpired(jwtUtil);
    }

    public void saveRefreshToken(RefreshToken refreshToken) {

        this.refreshToken = refreshToken;
    }

    public void invalidateRefreshToken() {

        this.refreshToken = null;
    }

    public boolean checkOwner(Long userId) {
        return this.id == userId;
    }
}
