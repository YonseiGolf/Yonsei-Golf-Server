package yonseigolf.server.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.payload.JsonFieldType;
import yonseigolf.server.docs.utils.RestDocsSupport;
import yonseigolf.server.user.controller.UserController;
import yonseigolf.server.user.dto.request.KakaoCode;
import yonseigolf.server.user.dto.request.SignUpUserRequest;
import yonseigolf.server.user.dto.response.KakaoLoginResponse;
import yonseigolf.server.user.dto.token.KakaoOauthInfo;
import yonseigolf.server.user.dto.token.OauthToken;
import yonseigolf.server.user.service.OauthLoginService;
import yonseigolf.server.user.service.UserService;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static yonseigolf.server.docs.utils.ApiDocumentUtils.getDocumentRequest;
import static yonseigolf.server.docs.utils.ApiDocumentUtils.getDocumentResponse;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest extends RestDocsSupport {

    @Mock
    private UserService userService;
    @Mock
    private OauthLoginService oauthLoginService;
    @Mock
    private KakaoOauthInfo kakaoOauthInfo;

    @Test
    @DisplayName("카카오톡 로그인을 할 수 있다.")
    void kakaoOauthLoginTest() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();

        KakaoCode kakaoCode = KakaoCode.builder()
                .kakaoCode("kakaoCode")
                .build();

        OauthToken oauthToken = OauthToken.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .expiresIn(1000L)
                .tokenType("tokenType")
                .build();

        KakaoLoginResponse kakaoLoginResponse = KakaoLoginResponse.builder()
                .id(1L)
                .build();

        given(oauthLoginService.getOauthToken(kakaoCode.getKakaoCode(), kakaoOauthInfo)).willReturn(oauthToken);
        given(oauthLoginService.processKakaoLogin(oauthToken.getAccessToken(), kakaoOauthInfo.getLoginUri())).willReturn(kakaoLoginResponse);

        // when


        // then
        mockMvc.perform(
                        post("/oauth/kakao")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(kakaoCode.getKakaoCode()))
                                .session(session)
                ).andDo(print())
                .andExpect(status().isOk())
                .andDo(document("kakao-login-doc",
                        getDocumentRequest(),
                        getDocumentResponse()
                ));
    }

    @Test
    @DisplayName("카톡 로그인 후 연세 골프 로그인을 할 수 있다.")
    void yonseiGolfLoginTest() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();

        // when

        // then
        mockMvc.perform(post("/users/signIn")
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("user-login-doc",
                        getDocumentRequest(),
                        getDocumentResponse()
                ));
    }

    @Test
    @DisplayName("회원이 아니라면 회원가입을 할 수 있다.")
    void signUpTest() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("kakaoUser", 1L);
        Long kakaoId = 1L;
        SignUpUserRequest request = SignUpUserRequest.builder()
                .name("name")
                .phoneNumber("phoneNumber")
                .major("major")
                .studentId(1)
                .age(1)
                .semester(10)
                .build();
        // when

        // then
        mockMvc.perform(post("/users/signUp")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("user-signUp-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING)
                                        .description("이름"),
                                fieldWithPath("phoneNumber").type(JsonFieldType.STRING)
                                        .description("전화번호"),
                                fieldWithPath("major").type(JsonFieldType.STRING)
                                        .description("전공"),
                                fieldWithPath("studentId").type(JsonFieldType.NUMBER)
                                        .description("학번"),
                                fieldWithPath("age").type(JsonFieldType.NUMBER)
                                        .description("나이"),
                                fieldWithPath("semester").type(JsonFieldType.NUMBER)
                                        .description("학기"))
                ));
    }

    @Override
    protected Object initController() {
        return new UserController(userService, oauthLoginService, kakaoOauthInfo);
    }
}
