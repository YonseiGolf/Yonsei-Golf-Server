package yonseigolf.server.user.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.payload.JsonFieldType;
import yonseigolf.server.apply.dto.response.SingleApplicationResult;
import yonseigolf.server.docs.utils.RestDocsSupport;
import yonseigolf.server.user.dto.request.KakaoCode;
import yonseigolf.server.user.dto.request.SignUpUserRequest;
import yonseigolf.server.user.dto.response.*;
import yonseigolf.server.user.dto.token.KakaoOauthInfo;
import yonseigolf.server.user.dto.token.OauthToken;
import yonseigolf.server.user.entity.UserRole;
import yonseigolf.server.user.service.OauthLoginService;
import yonseigolf.server.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
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
        SessionUser user = SessionUser.builder()
                .id(1L)
                .name("name")
                .adminStatus(true)
                .build();
        session.setAttribute("kakaoUser", 1L);

        // when
        given(userService.signIn(1L)).willReturn(user); // userService mocking

        // then
        mockMvc.perform(post("/users/signIn")
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("user-login-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(JsonFieldType.NUMBER)
                                        .description("유저 id"),
                                fieldWithPath("name").type(JsonFieldType.STRING)
                                        .description("유저 이름"),
                                fieldWithPath("adminStatus").type(JsonFieldType.BOOLEAN)
                                        .description("관리자 여부")))

                );
    }

    @Test
    @DisplayName("회원이 아니라면 회원가입을 할 수 있다.")
    void signUpTest() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("kakaoUser", 1L);
        SignUpUserRequest request = SignUpUserRequest.builder()
                .name("name")
                .phoneNumber("phoneNumber")
                .major("major")
                .studentId(1)
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
                                fieldWithPath("semester").type(JsonFieldType.NUMBER)
                                        .description("학기"))
                ));
    }

    @Test
    @DisplayName("회장 및 부회장 정보 조회 테스트")
    void getLeadersTest() throws Exception {
        // given
        AdminResponse mockAdminResponse = AdminResponse.builder()
                .leader(UserResponse.builder()
                        .name("name")
                        .phoneNumber("phoneNumber")
                        .role(UserRole.LEADER)
                        .build())
                .assistantLeaders(List.of(
                        UserResponse.builder()
                                .name("name")
                                .phoneNumber("phoneNumber")
                                .role(UserRole.ASSISTANT_LEADER)
                                .build()
                ))
                .build();
        given(userService.getLeaders()).willReturn(mockAdminResponse);

        // when & then
        mockMvc.perform(get("/users/leaders"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("get-leaders-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("leader.name").type(JsonFieldType.STRING)
                                        .description("회장 이름"),
                                fieldWithPath("leader.phoneNumber").type(JsonFieldType.STRING)
                                        .description("회장 전화번호"),
                                fieldWithPath("leader.role").type(JsonFieldType.STRING)
                                        .description("직책"),
                                fieldWithPath("assistantLeaders[].name").type(JsonFieldType.STRING)
                                        .description("부회장 이름"),
                                fieldWithPath("assistantLeaders[].phoneNumber").type(JsonFieldType.STRING)
                                        .description("부회장 전화번호"),
                                fieldWithPath("assistantLeaders[].role").type(JsonFieldType.STRING)
                                        .description("직책")
                        )
                ));
    }

    @Test
    @DisplayName("모든 회원을 조회할 수 있다.")
    void findAllUserTest() throws Exception {
        // given
        List<SingleUserResponse> users = List.of(
                SingleUserResponse.builder()
                        .id(1L)
                        .name("name")
                        .phoneNumber("phoneNumber")
                        .major("major")
                        .studentId(1)
                        .semester(10)
                        .role(UserRole.MEMBER)
                        .build()
        );

        Page<SingleUserResponse> mockPage = new PageImpl<>(users);
        given(userService.findAllUsers(any())).willReturn(mockPage);
        given(userService.findAllUsers(any())).willReturn(mockPage);

        // when
        userService.findAllUsers(null);

        // then
        mockMvc.perform(get("/admin/users")
                        .param("page", "0")
                        .param("offset", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("find-all-users-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("content[].id").type(JsonFieldType.NUMBER)
                                        .description("유저 id"),
                                fieldWithPath("content[].kakaoId").type(JsonFieldType.NUMBER)
                                        .description("유저 카카오 id"),
                                fieldWithPath("content[].name").type(JsonFieldType.STRING)
                                        .description("유저 이름"),
                                fieldWithPath("content[].phoneNumber").type(JsonFieldType.STRING)
                                        .description("유저 전화번호"),
                                fieldWithPath("content[].studentId").type(JsonFieldType.NUMBER)
                                        .description("유저 학번"),
                                fieldWithPath("content[].major").type(JsonFieldType.STRING)
                                        .description("유저 전공"),
                                fieldWithPath("content[].semester").type(JsonFieldType.NUMBER)
                                        .description("유저 기수"),
                                fieldWithPath("content[].role").type(JsonFieldType.STRING)
                                        .description("유저 권한"),
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
                                        .description("페이지가 비어있는지 여부"))));
    }


    @Override
    protected Object initController() {
        return new UserController(userService, oauthLoginService, kakaoOauthInfo);
    }
}
