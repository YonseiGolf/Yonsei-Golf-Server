package yonseigolf.server.apply.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yonseigolf.server.apply.dto.request.EmailAlertRequest;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAlarm {

    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private Long id;
    private String email;

    public static EmailAlarm of(EmailAlertRequest request) {

        return EmailAlarm.builder()
                .email(request.getEmail())
                .build();
    }
}
