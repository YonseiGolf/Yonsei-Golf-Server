package yonseigolf.server.email.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailSMTPRequest {

    private String to;
    private String subject;
    private String body;
}
