package com.vetleDemo.store;

import com.vetleDemo.store.service.BankingApiService;
import com.vetleDemo.store.service.OAuthService;
import com.vetleDemo.store.model.TokenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OAuthService oauthService;

    @MockBean
    private BankingApiService bankingApiService;

    @Test
    void callbackSuccessfullyHandlesOAuthFlow() throws Exception {
        // Given
        String code = "test-code";
        String state = "test-state";

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken("test-access-token");
        tokenResponse.setTokenType("Bearer");
        tokenResponse.setExpiresIn(3600);

        String accountJson = "{\"accounts\": [{\"id\": \"123\"}]}";

        when(oauthService.exchangeCodeForToken(code, state)).thenReturn(tokenResponse);
        when(bankingApiService.getAccounts("test-access-token")).thenReturn(accountJson);

        // When & Then
        mockMvc.perform(get("/callback")
                        .param("code", code)
                        .param("state", state))
                .andExpect(status().isOk())
                .andExpect(content().string(accountJson));
    }
}

