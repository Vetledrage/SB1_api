package com.vetleDemo.store.service;

import com.vetleDemo.store.config.SparebankOauthConfig;
import com.vetleDemo.store.model.TokenResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SparebankOauthConfig oauthConfig;

    @InjectMocks
    private OAuthService oauthService;

    @Test
    void exchangeCodeForTokenSuccessfully() {
        // Given
        String code = "test-code";
        String state = "test-state";
        String tokenJson = "{" +
                "\"access_token\":\"test-token\"," +
                "\"token_type\":\"Bearer\"," +
                "\"expires_in\":3600," +
                "\"refresh_token\":\"test-refresh\"," +
                "\"refresh_token_expires_in\":7200" +
                "}";

        when(oauthConfig.getClientId()).thenReturn("test-client-id");
        when(oauthConfig.getClientSecret()).thenReturn("test-client-secret");
        when(oauthConfig.getRedirectUri()).thenReturn("http://localhost:8080/callback");
        when(oauthConfig.getTokenUri()).thenReturn("https://example.test/oauth/token");
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn(tokenJson);

        // When
        TokenResponse response = oauthService.exchangeCodeForToken(code, state);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("test-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600);
    }

    @Test
    void exchangeCodeForTokenThrowsOAuthException() {
        // Given
        when(oauthConfig.getClientId()).thenReturn("test-client-id");
        when(oauthConfig.getClientSecret()).thenReturn("test-client-secret");
        when(oauthConfig.getRedirectUri()).thenReturn("http://localhost:8080/callback");
        when(oauthConfig.getTokenUri()).thenReturn("https://example.test/oauth/token");
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Network error"));

        // Then
        assertThatThrownBy(() -> oauthService.exchangeCodeForToken("code", "state"))
                .isInstanceOf(OAuthException.class)
                .hasMessageContaining("Token exchange failed");
    }
}

