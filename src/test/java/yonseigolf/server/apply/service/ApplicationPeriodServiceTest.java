package yonseigolf.server.apply.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import yonseigolf.server.apply.dto.response.RecruitPeriodResponse;
import yonseigolf.server.apply.entity.RecruitmentPeriod;
import yonseigolf.server.apply.repository.ApplyPeriodRepository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Transactional
@SpringBootTest
class ApplicationPeriodServiceTest {

    @Autowired
    private ApplyPeriodRepository applyPeriodRepository;
    @Autowired
    private ApplyPeriodService applyPeriodService;

    @Test
    @DisplayName("지원 기간을 조회할 수 있다.")
    void applyPeriodTest() {
        // given
        RecruitPeriodResponse result = RecruitPeriodResponse.builder()
                .startDate(LocalDate.of(2021, 8, 1))
                .endDate(LocalDate.of(2021, 8, 1))
                .firstResultDate(LocalDate.of(2021, 8, 1))
                .interviewStartDate(LocalDate.of(2021, 8, 1))
                .interviewEndDate(LocalDate.of(2021, 8, 1))
                .finalResultDate(LocalDate.of(2021, 8, 1))
                .orientationDate(LocalDate.of(2021, 8, 1))
                .build();

        RecruitmentPeriod recruitmentPeriod = RecruitmentPeriod.builder()
                .startDate(LocalDate.of(2021, 8, 1))
                .endDate(LocalDate.of(2021, 8, 1))
                .firstResultDate(LocalDate.of(2021, 8, 1))
                .interviewStartDate(LocalDate.of(2021, 8, 1))
                .interviewEndDate(LocalDate.of(2021, 8, 1))
                .finalResultDate(LocalDate.of(2021, 8, 1))
                .orientationDate(LocalDate.of(2021, 8, 1))
                .build();

        RecruitmentPeriod saved = applyPeriodRepository.save(recruitmentPeriod);

        // when
        RecruitPeriodResponse applicationPeriod = applyPeriodService.getApplicationPeriod(saved.getId());
        // then
        assertAll(
                () -> assertThat(applicationPeriod.getStartDate()).isEqualTo(result.getStartDate()),
                () -> assertThat(applicationPeriod.getEndDate()).isEqualTo(result.getEndDate()),
                () -> assertThat(applicationPeriod.getFirstResultDate()).isEqualTo(result.getFirstResultDate()),
                () -> assertThat(applicationPeriod.getInterviewStartDate()).isEqualTo(result.getInterviewStartDate()),
                () -> assertThat(applicationPeriod.getInterviewEndDate()).isEqualTo(result.getInterviewEndDate()),
                () -> assertThat(applicationPeriod.getFinalResultDate()).isEqualTo(result.getFinalResultDate()),
                () -> assertThat(applicationPeriod.getOrientationDate()).isEqualTo(result.getOrientationDate())
        );
    }
}
