package yonseigolf.server.apply.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer semester;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate firstResultDate;
    private LocalDate finalResultDate;
    private LocalDate interviewStartDate;
    private LocalDate interviewEndDate;
    private LocalDate orientationDate;

    public void update(Integer semester, LocalDate startDate, LocalDate endDate,
                       LocalDate firstResultDate, LocalDate finalResultDate,
                       LocalDate interviewStartDate, LocalDate interviewEndDate,
                       LocalDate orientationDate) {
        this.semester = semester;
        this.startDate = startDate;
        this.endDate = endDate;
        this.firstResultDate = firstResultDate;
        this.finalResultDate = finalResultDate;
        this.interviewStartDate = interviewStartDate;
        this.interviewEndDate = interviewEndDate;
        this.orientationDate = orientationDate;
    }
}
