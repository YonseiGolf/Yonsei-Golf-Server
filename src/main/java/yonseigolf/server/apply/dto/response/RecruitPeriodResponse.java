package yonseigolf.server.apply.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class RecruitPeriodResponse {

    @JsonFormat(pattern = "MM월dd일")
    private LocalDate startDate;
    @JsonFormat(pattern = "MM월dd일")
    private LocalDate endDate;
    @JsonFormat(pattern = "MM월dd일")
    private LocalDate firstResultDate;
    @JsonFormat(pattern = "MM월dd일")
    private LocalDate interviewStartDate;
    @JsonFormat(pattern = "MM월dd일")
    private LocalDate interviewEndDate;
    @JsonFormat(pattern = "MM월dd일")
    private LocalDate finalResultDate;
    @JsonFormat(pattern = "MM월dd일")
    private LocalDate orientationDate;
}
