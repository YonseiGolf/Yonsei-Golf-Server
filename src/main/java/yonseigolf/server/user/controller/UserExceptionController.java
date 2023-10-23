package yonseigolf.server.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import yonseigolf.server.util.CustomResponse;

@Slf4j
@RestControllerAdvice
public class UserExceptionController {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CustomResponse> existingMember(IllegalArgumentException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new CustomResponse<>(
                        "fail",
                        401,
                        ex.getMessage()));
    }
}
