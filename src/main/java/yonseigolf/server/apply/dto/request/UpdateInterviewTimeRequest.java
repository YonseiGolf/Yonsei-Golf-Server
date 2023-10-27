package yonseigolf.server.apply.dto.request;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UpdateInterviewTimeRequest {

    private LocalDateTime time;
}
