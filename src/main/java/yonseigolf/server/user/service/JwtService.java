package yonseigolf.server.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;

@Slf4j
@Component
public class JwtService {

    private static final String USER_PROFILE = "userProfile";
    @Value("${JWT_SECRET_KEY}")
    private String secret; // 시크릿 키를 설정

    public<T> String createToken(T loggedInUser, Date expiredDate) {

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject("login_member")
                .claim(USER_PROFILE, loggedInUser)
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public String createRefreshToken(Long userId, Date expireDate) {

        // refresh token 만료 기한은 2주일
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject("refresh_token")
                .claim(USER_PROFILE, userId)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    // token이 만료되면 false 반환
    public boolean validateTokenIsExpired(String token) {

        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token);

            return !claimsJws.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }


    public boolean validateTokenIsManipulated(String token) {

        try {
            byte[] decodedSecretKey = Base64.getDecoder().decode(secret);
            Key key = new SecretKeySpec(decodedSecretKey, 0, decodedSecretKey.length, "HmacSHA256");

            Jwts.parser()
                    .setSigningKey(key) // 비밀 키를 사용하여 서명을 검증
                    .parseClaimsJws(token);

            return true;
        } catch (JwtException e) {
            log.info(e.getMessage() + " 토근 검증 실패");
            return false;
        }
    }
    public <T> T extractedUserFromToken(String token, Class<T> clazz) {
        String[] jwtParts = token.split("\\.");
        String encodedPayload = jwtParts[1]; // 페이로드는 두 번째 부분

        byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedPayload);
        String decodedPayload = new String(decodedBytes);

        ObjectMapper objectMapper = new ObjectMapper();

        return parseUserInfoFromJwt(decodedPayload, objectMapper, clazz);
    }

    private <T> T parseUserInfoFromJwt(String decodedPayload, ObjectMapper objectMapper, Class<T> clazz) {
        try {
            LinkedHashMap payloadMap = objectMapper.readValue(decodedPayload, LinkedHashMap.class);
            Object userProfile = payloadMap.get(USER_PROFILE);
            String userProfileJson = objectMapper.writeValueAsString(userProfile); // JSON 문자열로 변환

            return objectMapper.readValue(userProfileJson, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("로그인 유저 정보를 가져올 수 없습니다.");
        }
    }
}
