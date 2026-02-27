package com.vetleDemo.store.exception;

import com.vetleDemo.store.service.OAuthException;
import com.vetleDemo.store.service.BankingApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(OAuthException.class)
    public ResponseEntity<Map<String, String>> handleOAuthException(OAuthException e) {
        logger.error("OAuth error: {}", e.getMessage(), e);

        Map<String, String> error = new HashMap<>();
        error.put("error", "authentication_failed");
        error.put("message", "Failed to authenticate with Sparebank");
        error.put("details", e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(BankingApiException.class)
    public ResponseEntity<Map<String, String>> handleBankingApiException(BankingApiException e) {
        logger.error("Banking API error: {}", e.getMessage(), e);

        Map<String, String> error = new HashMap<>();
        error.put("error", "banking_api_failed");
        error.put("message", "Failed to fetch banking data");
        error.put("details", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception e) {
        logger.error("Unexpected error: {}", e.getMessage(), e);

        Map<String, String> error = new HashMap<>();
        error.put("error", "internal_server_error");
        error.put("message", "An unexpected error occurred");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

