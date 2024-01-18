package yonseigolf.server.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import yonseigolf.server.user.exception.*;
import yonseigolf.server.util.CustomErrorResponse;

@Slf4j
@RestControllerAdvice
public class UserExceptionController {

    /*
    40101 access token 이 없거나 만료됨, refresh token을 통한 access 토큰 재발급
    40102 refresh token이 없거나 만료됨, 로그인 페이지로 이동
    409 중복 로그인
     */

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CustomErrorResponse> existingMember(IllegalArgumentException ex) {

        log.error("existingMember: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new CustomErrorResponse(
                        "fail",
                        401,
                        ex.getMessage()
                ));
    }

    @ExceptionHandler({AccessTokenExpiredException.class, NoAuthorizationException.class})
    public ResponseEntity<CustomErrorResponse> accessTokenExpired(AccessTokenExpiredException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new CustomErrorResponse(
                        "fail",
                        40101,
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(NotBearerTypeException.class)
    public ResponseEntity<CustomErrorResponse> notBearerType(NotBearerTypeException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new CustomErrorResponse(
                        "fail",
                        401,
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(TokenManipulatedException.class)
    public ResponseEntity<CustomErrorResponse> tokenManipulated(TokenManipulatedException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new CustomErrorResponse(
                        "fail",
                        401,
                        ex.getMessage()
                ));
    }

    // refresh token이 만료된 경우 signIn 페이지로 이동
    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<CustomErrorResponse> refreshTokenExpired(RefreshTokenExpiredException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new CustomErrorResponse(
                        "fail",
                        40102,
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(DuplicatedLoginException.class)
    public ResponseEntity<CustomErrorResponse> duplicatedLogin(DuplicatedLoginException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new CustomErrorResponse(
                        "fail",
                        409,
                        ex.getMessage()
                ));
    }
}
