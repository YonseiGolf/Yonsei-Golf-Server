package yonseigolf.server.email.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import yonseigolf.server.apply.entity.EmailAlarm;
import yonseigolf.server.docs.utils.RestDocsSupport;
import yonseigolf.server.email.dto.response.AllWaitingEmail;
import yonseigolf.server.email.service.EmailService;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static yonseigolf.server.docs.utils.ApiDocumentUtils.getDocumentRequest;
import static yonseigolf.server.docs.utils.ApiDocumentUtils.getDocumentResponse;

@ExtendWith(MockitoExtension.class)
public class EmailControllerTest extends RestDocsSupport {

    @Mock
    private EmailService emailService;

    @Test
    @DisplayName("지원 시작 이메일을 보낸다.")
    void applyStartMailTest() throws Exception {
        // given
        doNothing().when(emailService).sendApplyStartAlert();

        // when
        emailService.sendApplyStartAlert();

        // then
        mockMvc.perform(
                        post("/admin/email/apply-start-email")
                ).andDo(print())
                .andExpect(status().isOk())
                .andDo(document("email-apply-start-email",
                        getDocumentRequest(),
                        getDocumentResponse()));
    }

    @Test
    @DisplayName("지원 대기자 목록을 조회할 수 있다.")
    void applyWaitingList() throws Exception {
        // given
        AllWaitingEmail response = AllWaitingEmail.builder()
                .emailAlarms(List.of(
                        EmailAlarm.builder()
                                .id(1L)
                                .email("email@email.com")
                                .build()
                        )
                )
                .build();

        given(emailService.findAllWaitingEmail()).willReturn(response);
        // when

        // then
        mockMvc.perform(
                get("/admin/email/apply-start-email")
        ).andDo(print())
                .andExpect(status().isOk())
                .andDo(document("email-apply-waiting-list",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("emailAlarms").description("이메일 알림 목록"),
                                fieldWithPath("emailAlarms[].id").type(JsonFieldType.NUMBER)
                                        .description("이메일 알림 ID"),
                                fieldWithPath("emailAlarms[].email").type(JsonFieldType.STRING)
                                        .description("이메일")
                        )));
    }


    @Override
    protected Object initController() {
        return new EmailController(emailService);
    }
}
