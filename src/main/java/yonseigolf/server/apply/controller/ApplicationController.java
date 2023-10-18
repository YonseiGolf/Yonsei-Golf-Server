package yonseigolf.server.apply.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import yonseigolf.server.util.CustomResponse;

@Controller
public class ApplicationController {

    @PostMapping("/application")
    public ResponseEntity<CustomResponse> apply() {

        return ResponseEntity
                .ok()
                .body(new CustomResponse(
                        "success",
                        200,
                        "연세골프 지원서 제출 성공"
                ));
    }
}
