package com.vetleDemo.store.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BankingApiService {

    private static final Logger logger = LoggerFactory.getLogger(BankingApiService.class);
    private static final String ACCOUNTS_ENDPOINT = "https://api.sparebank1.no/personal/banking/accounts";
    private static final String ACCEPT_HEADER = "application/vnd.sparebank1.v1+json; charset=utf-8";

    @Autowired
    private RestTemplate restTemplate;

    public String getAccounts(String accessToken) {
        try {
            logger.info("Fetching accounts from Sparebank API");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", ACCEPT_HEADER);

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    ACCOUNTS_ENDPOINT,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            logger.info("Successfully fetched accounts");
            return response.getBody();

        } catch (Exception e) {
            logger.error("Failed to fetch accounts from Sparebank API", e);
            throw new BankingApiException("Failed to fetch accounts: " + e.getMessage(), e);
        }
    }
}

