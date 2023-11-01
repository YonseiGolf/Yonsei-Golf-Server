package yonseigolf.server.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import yonseigolf.server.util.CustomErrorResponse;

@Slf4j
@RestControllerAdvice
public class UserExceptionController {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CustomErrorResponse> existingMember(IllegalArgumentException ex) {

        log.error("existingMember: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new CustomErrorResponse(
                        "fail",
                        401,
                        "이미 가입된 회원입니다."
                ));
    }
}
