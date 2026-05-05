package com.vetleDemo.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);
    private static final String BASE = "https://api.sparebank1.no/personal/banking/accounts";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    // ── helpers ──────────────────────────────────────────────────────────────

    private HttpEntity<Void> authEntity(HttpSession session) {
        String token = (String) session.getAttribute("access_token");
        if (token == null) throw new IllegalStateException("Not authenticated – please log in again.");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Accept", "application/vnd.sparebank1.v1+json; charset=utf-8");
        return new HttpEntity<>(headers);
    }

    private ResponseEntity<String> get(String url, HttpSession session) {
        try {
            return restTemplate.exchange(url, HttpMethod.GET, authEntity(session), String.class);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // ── endpoints ─────────────────────────────────────────────────────────────

    /** GET /api/accounts  →  list all accounts */
    @GetMapping
    public ResponseEntity<String> getAll(HttpSession session) {
        return get(BASE, session);
    }

    /** GET /api/accounts/default */
    @GetMapping("/default")
    public ResponseEntity<String> getDefault(HttpSession session) {
        return get(BASE + "/default", session);
    }

    /** GET /api/accounts/keys */
    @GetMapping("/keys")
    public ResponseEntity<String> getKeys(HttpSession session) {
        return get(BASE + "/keys", session);
    }

    /** GET /api/accounts/{accountKey} */
    @GetMapping("/{accountKey}")
    public ResponseEntity<String> getByKey(@PathVariable String accountKey, HttpSession session) {
        return get(BASE + "/" + accountKey, session);
    }

    /** GET /api/accounts/{accountKey}/details */
    @GetMapping("/{accountKey}/details")
    public ResponseEntity<String> getDetails(@PathVariable String accountKey, HttpSession session) {
        return get(BASE + "/" + accountKey + "/details", session);
    }

    /** GET /api/accounts/{accountKey}/roles */
    @GetMapping("/{accountKey}/roles")
    public ResponseEntity<String> getRoles(@PathVariable String accountKey, HttpSession session) {
        return get(BASE + "/" + accountKey + "/roles", session);
    }

    /** GET /api/accounts/child/{id} */
    @GetMapping("/child/{id}")
    public ResponseEntity<String> getChild(@PathVariable String id, HttpSession session) {
        return get(BASE + "/child/" + id, session);
    }

    /** POST /api/accounts/balance  –  body: { "accountKeys": ["key1","key2"] } */
    @PostMapping("/balance")
    public ResponseEntity<String> getBalance(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            String token = (String) session.getAttribute("access_token");
            if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated.");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.set("Accept", "application/vnd.sparebank1.v1+json; charset=utf-8");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            return restTemplate.exchange(BASE + "/balance", HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }
}

