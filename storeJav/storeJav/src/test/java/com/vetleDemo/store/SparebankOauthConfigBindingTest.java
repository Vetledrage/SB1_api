package com.vetleDemo.store;

import com.vetleDemo.store.config.SparebankOauthConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = StoreApplication.class)
@TestPropertySource(properties = {
        "sparebank.oauth.client-id=test-client-id",
        "sparebank.oauth.client-secret=test-client-secret",
        "sparebank.oauth.redirect-uri=http://localhost/test-callback",
        "sparebank.oauth.token-uri=https://example.test/oauth/token"
})
class SparebankOauthConfigBindingTest {

    @Autowired
    private SparebankOauthConfig config;

    @Test
    void bindsConfigurationProperties() {
        assertThat(config.getClientId()).isEqualTo("test-client-id");
        assertThat(config.getClientSecret()).isEqualTo("test-client-secret");
        assertThat(config.getRedirectUri()).isEqualTo("http://localhost/test-callback");
        assertThat(config.getTokenUri()).isEqualTo("https://example.test/oauth/token");
    }
}

