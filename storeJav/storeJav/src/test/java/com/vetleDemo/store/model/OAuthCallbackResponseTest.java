package com.vetleDemo.store.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OAuthCallbackResponseTest {

    @Test
    void toStringContainsCodeAndState() {
        OAuthCallbackResponse resp = new OAuthCallbackResponse("c1", "s1");

        assertThat(resp.getCode()).isEqualTo("c1");
        assertThat(resp.getState()).isEqualTo("s1");
        assertThat(resp.toString()).contains("code='c1'");
        assertThat(resp.toString()).contains("state='s1'");
    }

    @Test
    void settersUpdateValues() {
        OAuthCallbackResponse resp = new OAuthCallbackResponse("c1", "s1");
        resp.setCode("c2");
        resp.setState("s2");

        assertThat(resp.getCode()).isEqualTo("c2");
        assertThat(resp.getState()).isEqualTo("s2");
    }
}

