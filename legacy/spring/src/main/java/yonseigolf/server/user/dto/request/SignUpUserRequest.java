package yonseigolf.server.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Size;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpUserRequest {

    @Size(min = 1, max = 10)
    private String name;
    @Size(min = 1, max = 15)
    private String phoneNumber;
    @Range(min = 1, max = 99)
    private int studentId;
    @Size(min = 1, max = 10)
    private String major;
    @Range(min = 1, max = 99)
    private int semester;
}
