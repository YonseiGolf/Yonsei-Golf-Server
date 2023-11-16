package yonseigolf.server.apply.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import yonseigolf.server.apply.dto.request.ApplicationRequest;
import yonseigolf.server.apply.dto.request.EmailAlertRequest;
import yonseigolf.server.apply.dto.response.ApplicationResponse;
import yonseigolf.server.apply.dto.response.SingleApplicationResult;
import yonseigolf.server.apply.entity.Application;
import yonseigolf.server.apply.entity.EmailAlarm;
import yonseigolf.server.apply.repository.ApplicationRepository;
import yonseigolf.server.apply.repository.EmailRepository;
import yonseigolf.server.email.service.EmailService;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class ApplyServiceTest {

    @Autowired
    private ApplyService applyService;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private EmailRepository emailRepository;
    @MockBean
    private EmailService emailService;

    @BeforeEach
    void setUp() {

        applicationRepository.deleteAll();
    }

    @Test
    @DisplayName("지원서를 제출할 수 있다.")
    void applyTest() {
        // given
        ApplicationRequest request = ApplicationRequest.builder()
                .email("email")
                .build();
        doNothing().when(emailService).sendEmail(any(), any(), any());

        // when
        applyService.apply(request);

        // then
        assertAll(
                () -> assertThat(applicationRepository.findAll()).hasSize(1),
                () -> verify(emailService, times(1)).sendEmail(any(), any(), any())
        );
    }

    @Test
    @DisplayName("이메일 알림 신청 테스트")
    void emailAlarmTest() {
        // given
        EmailAlertRequest request = EmailAlertRequest.builder()
                .email("email")
                .build();

        EmailAlarm saved = emailRepository.save(EmailAlarm.of(request));

        // when
        applyService.emailAlarm(request);

        // then
        assertThat(emailRepository.findById(saved.getId()).get().getEmail()).isEqualTo("email");
    }

    @ParameterizedTest
    @DisplayName("지원서 조회 테스트")
    @ValueSource(strings = {"true", "false", "null"})
    void getApplicationResultsTest(String documentPassStr) {
        // given
        Boolean documentPass = null;

        if(!documentPassStr.equals("null")) {
            documentPass = Boolean.parseBoolean(documentPassStr);
        }

        Boolean finalPass = true;
        Application application = Application.builder()
                .documentPass(documentPass)
                .finalPass(finalPass)
                .build();

        applicationRepository.save(application);

        // when
        Page<SingleApplicationResult> resultPage = applyService.getApplicationResults(documentPass, finalPass, PageRequest.of(0, 10));

        // then
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("지원서를 조회할 수 있다.")
    void getApplicationTest() {
        // given
        Application application = Application.builder()
                .age(20)
                .photo("photo")
                .name("name")
                .phoneNumber("phoneNumber")
                .email("email")
                .build();
        Application saved = applicationRepository.save(application);

        // when
        ApplicationResponse response = applyService.getApplication(saved.getId());

        // then
        assertAll(
                () -> assertThat(response.getAge()).isEqualTo(application.getAge()),
                () -> assertThat(response.getPhoto()).isEqualTo(application.getPhoto()),
                () -> assertThat(response.getName()).isEqualTo(application.getName()),
                () -> assertThat(response.getPhoneNumber()).isEqualTo(application.getPhoneNumber()),
                () -> assertThat(response.getEmail()).isEqualTo(application.getEmail())
        );
    }
    
    @Test
    @DisplayName("면접 시간을 업데이트할 수 있다.")
    void interviewUpdateTest() {
        // given
        Application application = Application.builder()
                .interviewTime(null)
                .build();
        Application saved = applicationRepository.save(application);
        LocalDateTime updateInterviewTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        // when
        applyService.updateInterviewTime(saved.getId(), updateInterviewTime);
        Application updated = applicationRepository.findById(saved.getId()).get();

        // then
        assertThat(updated.getInterviewTime()).isEqualTo(updateInterviewTime);
    }

    @Test
    @DisplayName("지원서가 존재하지 않는데 인터뷰 시간을 업데이트 하려고 하면 예외가 발생한다.")
    void test() {
        // given
        Application application = Application.builder().build();
        applicationRepository.save(application);
        Long notExistId = 100L;

        // when & then
        assertThatThrownBy(() -> applyService.updateInterviewTime(notExistId, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 지원서가 존재하지 않습니다.");

    }

    @ParameterizedTest
    @DisplayName("서류 및 최종 합격 결과에 따른 이메일 알림을 전송 한다.")
    @CsvSource({"true,null", "true,true", "true,false", "false,false"})
    void sendEmailNotificationTest(boolean isDocumentPass, String finalPass) {
        // given
        Boolean isFinalPass = null;
        if (!finalPass.equals("null")) {
            isFinalPass = Boolean.parseBoolean(finalPass);
        }
        Application application = Application.builder()
                .name("name")
                .email("email")
                .documentPass(isDocumentPass)
                .finalPass(isFinalPass)
                .build();

        System.out.println("isDocumentPass = " + isDocumentPass);
        System.out.println("isFinalPass = " + isFinalPass);
        System.out.println((isDocumentPass && isFinalPass == null));


        applicationRepository.save(application);
        int expected = applicationRepository.findAll().size();

        // when
        applyService.sendEmailNotification(isDocumentPass, isFinalPass);

        // then
        verify(emailService, times(expected)).sendEmail(any(), any(), any());
    }
}

