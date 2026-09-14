package yonseigolf.server.email.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yonseigolf.server.apply.entity.EmailAlarm;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllWaitingEmail {

    private List<EmailAlarm> emailAlarms;
}
