package yonseigolf.server.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import yonseigolf.server.docs.utils.RestDocsSupport;
import yonseigolf.server.user.dto.request.KakaoCode;
import yonseigolf.server.user.dto.request.UserClassRequest;
import yonseigolf.server.user.dto.response.*;
import yonseigolf.server.user.dto.token.KakaoOauthInfo;
import yonseigolf.server.user.dto.token.OauthToken;
import yonseigolf.server.user.entity.UserClass;
import yonseigolf.server.user.entity.UserRole;
import yonseigolf.server.user.service.JwtService;
import yonseigolf.server.user.service.OauthLoginService;
import yonseigolf.server.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static yonseigolf.server.docs.utils.ApiDocumentUtils.getDocumentRequest;
import static yonseigolf.server.docs.utils.ApiDocumentUtils.getDocumentResponse;

@ExtendWith(MockitoExtension.class)
class UserControllerTest extends RestDocsSupport {

    @Mock
    private UserService userService;
    @Mock
    private OauthLoginService oauthLoginService;
    @Mock
    private KakaoOauthInfo kakaoOauthInfo;
    @Mock
    private JwtService jwtUtil;

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
        LoggedInUser user = LoggedInUser.builder()
                .id(1L)
                .name("name")
                .adminStatus(true)
                .build();

        given(userService.signIn(any())).willReturn(user);
        given(jwtUtil.createToken(any(), any())).willReturn("token");

        // when & then
        mockMvc.perform(
                        post("/users/signIn")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("user-login-doc",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("accessToken").type(JsonFieldType.STRING)
                                        .description("JWT Access Token")
                        )

                ));
    }

    @Test
    @DisplayName("로그인된 상태라면 에러를 발생한다.")
    void loggedInErrorTest() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", LoggedInUser.builder().id(1L).build());

        // when & then
        mockMvc.perform(post("/users/signUp").session(session))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertFalse(result.getResolvedException() instanceof IllegalArgumentException));
    }

    @Test
    @DisplayName("카카오 로그인을 하지 않고 회원가입을 할 경우 에러를 발생한다.")
    void kakaoLoginErrorTest() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();

        // when & then
        mockMvc.perform(post("/users/signUp").session(session))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertFalse(result.getResolvedException() instanceof IllegalArgumentException));
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
                        .userClass(UserClass.NONE)
                        .build()
        );

        Page<SingleUserResponse> mockPage = new PageImpl<>(users);
        given(userService.findUsersByClass(any(), any())).willReturn(mockPage);
        given(userService.findUsersByClass(any(), any())).willReturn(mockPage);

        // when
        userService.findUsersByClass(any(), any());

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
                                fieldWithPath("content[].userClass").type(JsonFieldType.STRING)
                                        .description("유저 등급"),
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

    @Test
    @DisplayName("사용자의 class를 업데이트 할 수 있다.")
    void updateUserClassTest() throws Exception {
        // given
        Long userId = 1L;
        UserClassRequest request = new UserClassRequest(UserClass.YB);
        doNothing().when(userService).updateUserClass(any(), any());

        // when

        // then
        mockMvc.perform(patch("/admin/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("patch-userClass-doc",
                        requestFields(
                                fieldWithPath("userClass").type(JsonFieldType.STRING)
                                        .description("유저 구분 (YB, OB, NONE 중 하나)")
                        )
                ));
    }

    @Test
    @DisplayName("로그아웃 요청 시 쿠키를 무효화하고 정상적인 응답을 반환한다.")
    void whenLoggingOut_ShouldInvalidateCookie_AndReturnSuccess() throws Exception {
        Long userId = 1L;

        mockMvc.perform(post("/users/logout")
                        .requestAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken")) // 쿠키가 존재하는지 확인
                .andExpect(cookie().maxAge("refreshToken", 0)); // 쿠키의 maxAge가 0인지 확인
    }

    @Test
    @DisplayName("Health check test")
    void healthCheck() throws Exception {

        mockMvc.perform(get("/healthcheck"))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("healthcheck-doc",
                        getDocumentRequest(),
                        getDocumentResponse()));
    }

    @Override
    protected Object initController() {
        return new UserController(userService, oauthLoginService, kakaoOauthInfo, jwtUtil);
    }
}
