package yonseigolf.server.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import yonseigolf.server.user.dto.request.SignUpUserRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class UserIntegrateControllerTest {

    private MockMvc mockMvc;
    @Autowired
    private UserController userController;
    @Autowired
    private UserExceptionController userExceptionController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(userExceptionController)
                .build();
    }

    @Test
    @DisplayName("이미 로그인한 경우 401 에러가 발생한다.")
    public void testAlreadyLoggedIn() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", "testUser");

        SignUpUserRequest request = SignUpUserRequest.builder()
                .name("testName")
                .phoneNumber("010-1234-5678")
                .studentId(12)
                .major("testMajor")
                .semester(1)
                .build();

        String requestBody = new ObjectMapper().writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/users/signUp")
                        .content(requestBody)
                        .contentType("application/json")
                        .session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그인 하지 않았지만, 카카오 로그인을 하지 않은 경우 401 에러가 발생한다.")
    void test() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();

        SignUpUserRequest request = SignUpUserRequest.builder()
                .name("testName")
                .phoneNumber("010-1234-5678")
                .studentId(12)
                .major("testMajor")
                .semester(1)
                .build();

        String requestBody = new ObjectMapper().writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/users/signUp")
                        .content(requestBody)
                        .contentType("application/json")
                        .session(session))
                .andExpect(status().isUnauthorized());
    }
}