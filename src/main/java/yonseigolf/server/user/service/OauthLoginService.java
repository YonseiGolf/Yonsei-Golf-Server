package yonseigolf.server.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import yonseigolf.server.user.dto.response.KakaoLoginResponse;
import yonseigolf.server.user.dto.token.KakaoOauthInfo;
import yonseigolf.server.user.dto.token.OauthToken;

import java.util.HashMap;
import java.util.Map;

@Service
public class OauthLoginService {

    private final RestTemplate restTemplate;

    @Autowired

    public OauthLoginService(RestTemplate restTemplate) {

        this.restTemplate = restTemplate;
    }

    public OauthToken getOauthToken(String code, KakaoOauthInfo oauthInfo) {
        HttpEntity<?> request = createRequestEntity(code, oauthInfo);
        ResponseEntity<OauthToken> response = restTemplate.postForEntity(oauthInfo.getRedirectUri(), request, OauthToken.class);

        return response.getBody();
    }

    public KakaoLoginResponse processKakaoLogin(String accessToken, String loginUri) {
        return processLogin(accessToken, loginUri);
    }

    private HttpEntity<?> createRequestEntity(String code, KakaoOauthInfo oauthInfo) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        Map<String, String> header = new HashMap<>();
        header.put("Accept", "application/json");
        header.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.setAll(header);

        MultiValueMap<String, String> requestPayloads = new LinkedMultiValueMap<>();
        Map<String, String> requestPayload = new HashMap<>();
        requestPayload.put("grant_type", "authorization_code");
        requestPayload.put("client_id", oauthInfo.getClientId());
        requestPayload.put("client_secret", oauthInfo.getClientSecret());
        requestPayload.put("code", code);
        requestPayloads.setAll(requestPayload);

        return new HttpEntity<>(requestPayloads, headers);
    }

    private KakaoLoginResponse processLogin(String accessToken, String loginUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                        loginUri,
                        HttpMethod.GET,
                        requestEntity,
                        KakaoLoginResponse.class)
                .getBody();
    }
}
