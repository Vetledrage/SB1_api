package com.vetleDemo.store;

import com.vetleDemo.store.config.SparebankOauthConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Controller
public class AuthController {

    @Autowired
    private SparebankOauthConfig oauthConfig;

    @GetMapping("/callback")
    public ResponseEntity<String> handleCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state
    ) {
        try {
            // Curl request
            ProcessBuilder tokenPb = new ProcessBuilder(
                    "curl",
                    "-sS",
                    "--location",
                    "--request", "POST", "https://api.sparebank1.no/oauth/token",
                    "--header", "Content-Type: application/x-www-form-urlencoded",
                    "--data-urlencode", "client_id=c3594748-3359-42ad-910f-7f31317900d8",
                    "--data-urlencode", "client_secret=42ebd1e8-0aec-4247-8a60-d7dae8cb46c9",
                    "--data-urlencode", "code=" + code,
                    "--data-urlencode", "grant_type=authorization_code",
                    "--data-urlencode", "state=" + state,
                    "--data-urlencode", "redirect_uri=http://localhost:8080/callback"
            );
            tokenPb.redirectErrorStream(true);
            Process tokenProc = tokenPb.start();

            StringBuilder tokenJson = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(tokenProc.getInputStream()))) {
                for (String line; (line = r.readLine()) != null; ) {
                    tokenJson.append(line);
                }
            }
            int tokenExit = tokenProc.waitFor();
            if (tokenExit != 0) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body("Token request failed: " + tokenJson);
            }

            // Extract access_token from JSON
            String accessToken = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readTree(tokenJson.toString())
                    .get("access_token").asText();

            //Call response 
            ProcessBuilder accountPb = new ProcessBuilder(
                    "curl",
                    "-sS",
                    "https://api.sparebank1.no/personal/banking/accounts",
                    "--header", "Authorization: Bearer " + accessToken,
                    "--header", "Accept: application/vnd.sparebank1.v1+json; charset=utf-8"
            );
            accountPb.redirectErrorStream(true);
            Process helloProc = accountPb.start();

            StringBuilder accountJson = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(helloProc.getInputStream()))) {
                for (String line; (line = r.readLine()) != null; ) {
                    accountJson.append(line);
                }
            }
            int helloExit = helloProc.waitFor();
            if (helloExit != 0) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body("HelloWorld request failed: " + accountJson);
            }
            return ResponseEntity.ok(accountJson.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong: " + e.getMessage());
        }
    }

}



