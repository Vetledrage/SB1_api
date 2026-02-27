package com.vetleDemo.store.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vetleDemo.store.config.SparebankOauthConfig;
import com.vetleDemo.store.model.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class OAuthService {

    private static final Logger logger = LoggerFactory.getLogger(OAuthService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SparebankOauthConfig oauthConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public TokenResponse exchangeCodeForToken(String code, String state) {
        try {
            logger.info("Exchanging authorization code for access token");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", oauthConfig.getClientId());
            body.add("client_secret", oauthConfig.getClientSecret());
            body.add("code", code);
            body.add("grant_type", "authorization_code");
            body.add("state", state);
            body.add("redirect_uri", oauthConfig.getRedirectUri());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            String response = restTemplate.postForObject(
                    oauthConfig.getTokenUri(),
                    request,
                    String.class
            );

            TokenResponse tokenResponse = objectMapper.readValue(response, TokenResponse.class);
            logger.info("Successfully obtained access token");

            return tokenResponse;

        } catch (Exception e) {
            logger.error("Failed to exchange code for token", e);
            throw new OAuthException("Token exchange failed: " + e.getMessage(), e);
        }
    }
}

