package yonseigolf.server.email.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yonseigolf.server.docs.utils.RestDocsSupport;
import yonseigolf.server.email.service.EmailService;

import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
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


    @Override
    protected Object initController() {
        return new EmailController(emailService);
    }
}
