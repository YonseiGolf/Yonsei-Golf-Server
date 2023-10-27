package yonseigolf.server.apply.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInterviewTimeRequest {

    private LocalDateTime time;
}
