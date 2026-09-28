package yonseigolf.server.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import yonseigolf.server.user.dto.response.KakaoAccount;
import yonseigolf.server.user.dto.response.KakaoLoginResponse;
import yonseigolf.server.user.dto.token.KakaoOauthInfo;
import yonseigolf.server.user.dto.token.OauthToken;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
class OauthLoginServiceTest {

    @Autowired
    private OauthLoginService oauthLoginService;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    @DisplayName("카카오 토큰을 받아올 수 있다.")
    public void testGetOauthToken() {
        // given
        String code = "sampleCode";
        String sampleRedirectUri = "http://sampleRedirectUri.com";
        KakaoOauthInfo oauthInfo = KakaoOauthInfo.builder().redirectUri(sampleRedirectUri).build();
        OauthToken expectedToken = OauthToken.builder().build();
        ResponseEntity<OauthToken> mockedResponse = new ResponseEntity<>(expectedToken, HttpStatus.OK);

        given(restTemplate.postForEntity(anyString(), any(), eq(OauthToken.class))).willReturn(mockedResponse);

        // when
        OauthToken resultToken = oauthLoginService.getOauthToken(code, oauthInfo);

        // then
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(OauthToken.class));
        assertEquals(expectedToken, resultToken);
    }

    @Test
    @DisplayName("카카오 로그인을 진행할 수 있다.")
    void kakaoLoginTest() {
        // given
        String accessToken = "sampleAccessToken";
        String loginUri = "sampleLoginUri";
        KakaoLoginResponse expectedResponse = KakaoLoginResponse.builder()
                .id(1L)
                .kakaoAccount(
                        KakaoAccount
                                .builder()
                                .email("email")
                                .build()
                )
                .build();
        ResponseEntity<KakaoLoginResponse> mockResponseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        given(restTemplate.exchange(
                eq(loginUri),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KakaoLoginResponse.class)))
                .willReturn(mockResponseEntity);

        // when
        KakaoLoginResponse actualResponse = oauthLoginService.processKakaoLogin(accessToken, loginUri);


        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }
}
