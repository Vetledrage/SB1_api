package com.vetleDemo.store;

import com.vetleDemo.store.model.TokenResponse;
import com.vetleDemo.store.service.OAuthService;
import com.vetleDemo.store.service.BankingApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private OAuthService oauthService;

    @Autowired
    private BankingApiService bankingApiService;

    @GetMapping("/callback")
    public ResponseEntity<String> handleCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state
    ) {
        logger.info("Received OAuth callback with code: {}", code);

        try {
            // Exchange authorization code for access token
            TokenResponse tokenResponse = oauthService.exchangeCodeForToken(code, state);

            // Fetch accounts using the access token
            String accountData = bankingApiService.getAccounts(tokenResponse.getAccessToken());

            logger.info("Successfully completed OAuth flow and retrieved account data");
            return ResponseEntity.ok(accountData);

        } catch (Exception e) {
            logger.error("Error during OAuth callback handling", e);
            throw e;
        }
    }

}



