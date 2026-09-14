package yonseigolf.server.apply.dto.request;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityClubRequest {
    private String clubName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String role;
}
