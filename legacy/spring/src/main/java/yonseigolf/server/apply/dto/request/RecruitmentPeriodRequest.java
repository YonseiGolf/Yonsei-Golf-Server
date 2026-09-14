package yonseigolf.server.apply.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentPeriodRequest {

    private Integer semester;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate firstResultDate;
    private LocalDate finalResultDate;
    private LocalDate interviewStartDate;
    private LocalDate interviewEndDate;
    private LocalDate orientationDate;
}
