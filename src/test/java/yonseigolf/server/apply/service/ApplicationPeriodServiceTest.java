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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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

    @Test
    @DisplayName("오늘이 지원 기간 이전이라면 false를 반환")
    void getApplicationAvailabilityTest() {
        // given
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
        boolean applicationAvailability = applyPeriodService.getApplicationAvailability(LocalDate.now(), saved.getId());

        // then
        assertThat(applicationAvailability).isFalse();
    }

    @Test
    @DisplayName("오늘이 지원 기간 이전이라면 false를 반환")
    void getApplicationAvailabilityFalseTest() {
        // given
        RecruitmentPeriod recruitmentPeriod = RecruitmentPeriod.builder()
                .startDate(LocalDate.of(2024, 8, 1))
                .endDate(LocalDate.of(2024, 8, 1))
                .firstResultDate(LocalDate.of(2021, 8, 1))
                .interviewStartDate(LocalDate.of(2021, 8, 1))
                .interviewEndDate(LocalDate.of(2021, 8, 1))
                .finalResultDate(LocalDate.of(2021, 8, 1))
                .orientationDate(LocalDate.of(2021, 8, 1))
                .build();
        RecruitmentPeriod saved = applyPeriodRepository.save(recruitmentPeriod);

        // when
        boolean applicationAvailability = applyPeriodService.getApplicationAvailability(LocalDate.now(), saved.getId());

        // then
        assertThat(applicationAvailability).isFalse();
    }

    @Test
    @DisplayName("오늘이 지원 기간이라면 true를 반환")
    void getApplicationAvailabilityTrueTest() {
        // given
        LocalDate now = LocalDate.now();
        LocalDate endDate = now.plusDays(1);


        RecruitmentPeriod recruitmentPeriod = RecruitmentPeriod.builder()
                .startDate(now)
                .endDate(endDate)
                .firstResultDate(LocalDate.of(2021, 8, 1))
                .interviewStartDate(LocalDate.of(2021, 8, 1))
                .interviewEndDate(LocalDate.of(2021, 8, 1))
                .finalResultDate(LocalDate.of(2021, 8, 1))
                .orientationDate(LocalDate.of(2021, 8, 1))
                .build();

        RecruitmentPeriod saved = applyPeriodRepository.save(recruitmentPeriod);

        // when
        boolean applicationAvailability = applyPeriodService.getApplicationAvailability(now, saved.getId());

        // then
        assertThat(applicationAvailability).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 지원 기간을 찾을 시 에러가 발생한다.")
    void findByIdExceptionTest() {
        // given
        long notExistId = 100L;

        // when & then
        assertThatThrownBy(() -> applyPeriodService.getApplicationPeriod(notExistId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 모집기간이 존재하지 않습니다.");
    }
}
