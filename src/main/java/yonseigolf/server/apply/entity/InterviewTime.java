package yonseigolf.server.apply.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_period_id")
    private RecruitmentPeriod recruitmentPeriod;

    private LocalDateTime interviewDateTime;

    public void update(LocalDateTime interviewDateTime) {
        this.interviewDateTime = interviewDateTime;
    }
}
