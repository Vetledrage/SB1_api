package com.vetleDemo.store.model;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class TokenResponseJsonTest {

    @Autowired
    private JacksonTester<TokenResponse> jackson;

    @Test
    void deserializesFromOAuthTokenPayload() throws Exception {
        String json = "{" +
                "\"access_token\":\"abc\"," +
                "\"token_type\":\"Bearer\"," +
                "\"expires_in\":3600," +
                "\"refresh_token\":\"rt\"," +
                "\"refresh_token_expires_in\":7200" +
                "}";

        TokenResponse parsed = jackson.parseObject(json);

        assertThat(parsed.getAccessToken()).isEqualTo("abc");
        assertThat(parsed.getTokenType()).isEqualTo("Bearer");
        assertThat(parsed.getExpiresIn()).isEqualTo(3600);
        assertThat(parsed.getRefreshToken()).isEqualTo("rt");
        assertThat(parsed.getRefreshTokenExpiresIn()).isEqualTo(7200);
    }
}

