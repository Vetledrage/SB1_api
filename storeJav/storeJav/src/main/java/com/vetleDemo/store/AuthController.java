package com.vetleDemo.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vetleDemo.store.config.SparebankOauthConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private SparebankOauthConfig oauthConfig;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/debug/config")
    @org.springframework.web.bind.annotation.ResponseBody
    public String debugConfig() {
        String secret = oauthConfig.getClientSecret();
        String masked = secret != null && secret.length() > 4
                ? secret.substring(0, 2) + "****" + secret.substring(secret.length() - 2)
                : "(null or too short)";
        return "client_id=" + oauthConfig.getClientId()
                + "\nclient_secret=" + masked
                + "\nredirect_uri=" + oauthConfig.getRedirectUri()
                + "\ntoken_uri=" + oauthConfig.getTokenUri();
    }

    @GetMapping("/callback")
    public String handleCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            HttpSession session
    ) {
        try {
            // Build body manually — same as curl --data-urlencode, full control over encoding
            String bodyStr = "client_id="     + URLEncoder.encode(oauthConfig.getClientId(),     StandardCharsets.UTF_8)
                    + "&client_secret=" + URLEncoder.encode(oauthConfig.getClientSecret(), StandardCharsets.UTF_8)
                    + "&code="          + URLEncoder.encode(code,                          StandardCharsets.UTF_8)
                    + "&grant_type=authorization_code"
                    + "&redirect_uri="  + URLEncoder.encode(oauthConfig.getRedirectUri(),  StandardCharsets.UTF_8);
            if (state != null && !state.isBlank()) {
                bodyStr += "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
            }

            HttpHeaders tokenHeaders = new HttpHeaders();
            // Set exact content-type — no charset suffix that could confuse the server
            tokenHeaders.set(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

            log.info("Token request body (secret masked): {}",
                    bodyStr.replaceAll("client_secret=[^&]+", "client_secret=****"));

            ResponseEntity<String> tokenResponse;
            try {
                tokenResponse = restTemplate.postForEntity(
                        oauthConfig.getTokenUri(),
                        new HttpEntity<>(bodyStr, tokenHeaders),
                        String.class
                );
            } catch (HttpClientErrorException e) {
                log.error("Token HTTP error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
                String msg = URLEncoder.encode(e.getStatusCode() + " – " + e.getResponseBodyAsString(), StandardCharsets.UTF_8);
                return "redirect:/?error=" + msg;
            }

            log.info("Token response: {}", tokenResponse.getBody());
            JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());

            if (!tokenJson.has("access_token")) {
                log.error("Token response missing access_token: {}", tokenResponse.getBody());
                String msg = URLEncoder.encode("no access_token: " + tokenResponse.getBody(), StandardCharsets.UTF_8);
                return "redirect:/?error=" + msg;
            }

            session.setAttribute("access_token", tokenJson.get("access_token").asText());
            if (tokenJson.has("refresh_token")) {
                session.setAttribute("refresh_token", tokenJson.get("refresh_token").asText());
            }

            return "redirect:/dashboard.html";

        } catch (Exception e) {
            log.error("Callback error", e);
            String msg = URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "unknown error", StandardCharsets.UTF_8);
            return "redirect:/?error=" + msg;
        }
    }
}
