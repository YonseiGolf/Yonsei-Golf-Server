package yonseigolf.server.apply.entity;

import lombok.Builder;
import lombok.Getter;
import yonseigolf.server.apply.dto.request.EmailRequest;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Builder
public class EmailAlarm {

    @Id
    @GeneratedValue
    private Long id;
    private String email;

    public static EmailAlarm of(EmailRequest request) {

        return EmailAlarm.builder()
                .email(request.getEmail())
                .build();
    }
}
