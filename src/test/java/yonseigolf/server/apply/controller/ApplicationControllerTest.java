package yonseigolf.server.apply.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.restdocs.payload.JsonFieldType;
import yonseigolf.server.apply.dto.request.ApplicationRequest;
import yonseigolf.server.apply.dto.request.EmailAlertRequest;
import yonseigolf.server.apply.dto.response.RecruitPeriodResponse;
import yonseigolf.server.apply.dto.response.SingleApplicationResult;
import yonseigolf.server.apply.service.ApplyPeriodService;
import yonseigolf.server.apply.service.ApplyService;
import yonseigolf.server.docs.utils.RestDocsSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static yonseigolf.server.docs.utils.ApiDocumentUtils.getDocumentRequest;
import static yonseigolf.server.docs.utils.ApiDocumentUtils.getDocumentResponse;


@ExtendWith(MockitoExtension.class)
public class ApplicationControllerTest extends RestDocsSupport {

    @Mock
    private ApplyService applyService;
    @Mock
    private ApplyPeriodService applyPeriodService;

    @Override
    protected Object initController() {

        return new ApplicationController(applyService, applyPeriodService);
    }

    @Test
    @DisplayName("지원자는 지원서를 작성할 수 있다.")
    void applicationPostingTest() throws Exception {
        // given
        ApplicationRequest request = ApplicationRequest.builder()
                .name("홍길동")
                .photo("사진")
                .age(20L)
                .studentId(1L)
                .major("체육교육")
                .phoneNumber("010-1234-5678")
                .golfDuration(1L)
                .roundCount(1L)
                .lessonStatus(true)
                .clubStatus(true)
                .selfIntroduction("자기소개")
                .applyReason("지원동기")
                .skillEvaluation("실력평가")
                .golfMemory("골프추억")
                .otherClub("다른동아리활동")
                .swingVideo("스윙영상")
                .submitTime(LocalDateTime.now())
                .build();

        // when


        // then
        mockMvc.perform(post("/application")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType("application/json")
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("application-create-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
                                fieldWithPath("photo").type(JsonFieldType.STRING).description("사진"),
                                fieldWithPath("age").type(JsonFieldType.NUMBER).description("나이"),
                                fieldWithPath("studentId").type(JsonFieldType.NUMBER).description("학번"),
                                fieldWithPath("major").type(JsonFieldType.STRING).description("전공"),
                                fieldWithPath("phoneNumber").type(JsonFieldType.STRING).description("전화번호"),
                                fieldWithPath("golfDuration").type(JsonFieldType.NUMBER).description("골프경력"),
                                fieldWithPath("roundCount").type(JsonFieldType.NUMBER).description("라운드횟수"),
                                fieldWithPath("lessonStatus").type(JsonFieldType.BOOLEAN).description("레슨여부"),
                                fieldWithPath("clubStatus").type(JsonFieldType.BOOLEAN).description("클럽보유여부"),
                                fieldWithPath("selfIntroduction").type(JsonFieldType.STRING).description("자기소개"),
                                fieldWithPath("applyReason").type(JsonFieldType.STRING).description("지원동기"),
                                fieldWithPath("skillEvaluation").type(JsonFieldType.STRING).description("골프 자시 실력 평가"),
                                fieldWithPath("golfMemory").type(JsonFieldType.STRING).description("골프추억"),
                                fieldWithPath("otherClub").type(JsonFieldType.STRING).description("다른동아리활동 기술"),
                                fieldWithPath("swingVideo").type(JsonFieldType.STRING).description("스윙영상"),
                                fieldWithPath("submitTime").type(JsonFieldType.ARRAY).description("제출시간"))
                ));
    }

    @Test
    @DisplayName("사용자는 지원 기간 이메일 신청을 할 수 있다.")
    void emailAlarmTest() throws Exception {
        // given
        EmailAlertRequest request = EmailAlertRequest.builder()
                .email("email@email.com)")
                .build();

        // when


        // then
        mockMvc.perform(post("/application/emailAlarm")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("application-emailAlarm-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일")
                        )
                ));
    }

    @Test
    @DisplayName("지원 기간을 조회할 수 있다.")
    void recruitPeriodTest() throws Exception {
        // given
        RecruitPeriodResponse response = RecruitPeriodResponse.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .firstResultDate(LocalDate.now())
                .interviewStartDate(LocalDate.now())
                .interviewEndDate(LocalDate.now())
                .finalResultDate(LocalDate.now())
                .orientationDate(LocalDate.now())
                .build();

        // when

        given(applyPeriodService.getApplicationPeriod()).willReturn(response);

        // then
        mockMvc.perform(get("/application/recruit"))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("application-recruitPeriod-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("startDate")
                                        .description("지원 시작 날짜"),
                                fieldWithPath("endDate")
                                        .description("지원 마감 날짜"),
                                fieldWithPath("firstResultDate")
                                        .description("1차 합격 발표 날짜"),
                                fieldWithPath("interviewStartDate")
                                        .description("면접 시작 날짜"),
                                fieldWithPath("interviewEndDate")
                                        .description("면접 마감 날짜"),
                                fieldWithPath("finalResultDate")
                                        .description("최종 합격 발표 날짜"),
                                fieldWithPath("orientationDate")
                                        .description("입회식 날짜")
                        )));
    }

    @Test
    @DisplayName("지원 가능 여부를 조회할 수 있다.")
    void applicationAvailableTest() throws Exception {
        // given


        // when


        // then
        mockMvc.perform(get("/application/availability"))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("application-applicationAvailable-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER)
                                        .description("응답 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("응답 메시지"),
                                fieldWithPath("status").type(JsonFieldType.STRING)
                                        .description("응답 상태"),
                                fieldWithPath("data").type(JsonFieldType.BOOLEAN)
                                        .description("지원 가능 여부")
                        )));
    }

    @Test
    @DisplayName("지원서를 조회할 수 있다.")
    void getApplicationTest() throws Exception {
        // given
        List<SingleApplicationResult> mockResults = Arrays.asList(
                new SingleApplicationResult(
                        1L,
                        "photo",
                        "name",
                        LocalDateTime.now(),
                        true,
                        false
                )
        );
        Page<SingleApplicationResult> mockPage = new PageImpl<>(mockResults);
        given(applyService.getApplicationResults(any(), any(), any())).willReturn(mockPage);

        // when & then
        mockMvc.perform(get("/admin/application")
                        .param("documentPass", "true")
                        .param("finalPass", "false")
                        .param("page", "0")
                        .param("offset", "10"))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("admin-application-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("content[].id").type(JsonFieldType.NUMBER)
                                        .description("지원서 ID"),
                                fieldWithPath("content[].photo").type(JsonFieldType.STRING)
                                        .description("사진"),
                                fieldWithPath("content[].name").type(JsonFieldType.STRING)
                                        .description("이름"),
                                fieldWithPath("content[].interviewTime").type(JsonFieldType.STRING)
                                        .description("면접 시간"),
                                fieldWithPath("content[].documentPass").type(JsonFieldType.BOOLEAN)
                                        .description("서류 합격 여부"),
                                fieldWithPath("content[].finalPass").type(JsonFieldType.BOOLEAN)
                                        .description("최종 합격 여부"),
                                fieldWithPath("pageable").ignored(),
                                fieldWithPath("last").type(JsonFieldType.BOOLEAN)
                                        .description("마지막 페이지 여부"),
                                fieldWithPath("totalPages").type(JsonFieldType.NUMBER)
                                        .description("총 페이지 수"),
                                fieldWithPath("totalElements").type(JsonFieldType.NUMBER)
                                        .description("총 요소 수"),
                                fieldWithPath("size").type(JsonFieldType.NUMBER)
                                        .description("페이지 크기"),
                                fieldWithPath("number").type(JsonFieldType.NUMBER)
                                        .description("현재 페이지 번호"),
                                fieldWithPath("sort.empty").type(JsonFieldType.BOOLEAN)
                                        .description("정렬 정보가 없는지 여부"),
                                fieldWithPath("sort.sorted").type(JsonFieldType.BOOLEAN)
                                        .description("정렬됐는지 여부"),
                                fieldWithPath("sort.unsorted").type(JsonFieldType.BOOLEAN)
                                        .description("미정렬됐는지 여부"),
                                fieldWithPath("first").type(JsonFieldType.BOOLEAN)
                                        .description("첫 페이지인지 여부"),
                                fieldWithPath("numberOfElements").type(JsonFieldType.NUMBER)
                                        .description("현재 페이지의 요소 수"),
                                fieldWithPath("empty").type(JsonFieldType.BOOLEAN)
                                        .description("페이지가 비어있는지 여부")
                        )));
    }
}
