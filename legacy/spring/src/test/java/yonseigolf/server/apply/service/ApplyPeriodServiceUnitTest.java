package yonseigolf.server.apply.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yonseigolf.server.apply.entity.RecruitmentPeriod;
import yonseigolf.server.apply.repository.ApplyPeriodRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ApplyPeriodServiceUnitTest {

    @Mock
    private ApplyPeriodRepository repository;

    private ApplyPeriodService applyPeriodService;

    @BeforeEach
    void setUp() {
        applyPeriodService = new ApplyPeriodService(repository);
    }

    @Test
    @DisplayName("최신 기수의 지원 기간을 기준으로 지원 가능 여부를 반환한다.")
    void getLatestApplicationAvailabilityTest() {
        // given
        LocalDate today = LocalDate.of(2026, 8, 6);
        RecruitmentPeriod latestPeriod = RecruitmentPeriod.builder()
                .id(3L)
                .semester(16)
                .startDate(LocalDate.of(2026, 8, 5))
                .endDate(LocalDate.of(2026, 8, 19))
                .build();
        given(repository.findTopByOrderBySemesterDesc()).willReturn(Optional.of(latestPeriod));

        // when
        boolean applicationAvailability = applyPeriodService.getLatestApplicationAvailability(today);

        // then
        assertThat(applicationAvailability).isTrue();
    }
}
