package yonseigolf.server.apply.entity;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yonseigolf.server.apply.dto.request.EmailAlertRequest;

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
    private Integer semester;
    private LocalDateTime sentAt;

    public static EmailAlarm of(EmailAlertRequest request) {

        return EmailAlarm.builder()
            .email(request.getEmail())
            .semester(request.getSemester())
            .build();
    }

    public void markAsSent() {
        this.sentAt = LocalDateTime.now();
    }
}
