package yonseigolf.server.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yonseigolf.server.user.exception.RefreshTokenExpiredException;
import yonseigolf.server.user.service.JwtService;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String refreshToken;

    public void isBeforeExpired(JwtService jwtUtil) {

        if (jwtUtil.validateRefreshTokenIsExpired(this.refreshToken)) {

            throw new RefreshTokenExpiredException("Refresh Token이 만료되었습니다..");
        }
    }
}
