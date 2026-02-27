package com.vetleDemo.store.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankingApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BankingApiService bankingApiService;

    @Test
    void getAccountsSuccessfully() {
        // Given
        String accessToken = "test-access-token";
        String accountJson = "{\"accounts\": [{\"id\": \"123\", \"name\": \"Test Account\"}]}";

        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(accountJson, HttpStatus.OK));

        // When
        String response = bankingApiService.getAccounts(accessToken);

        // Then
        assertThat(response).isEqualTo(accountJson);
    }

    @Test
    void getAccountsThrowsBankingApiException() {
        // Given
        String accessToken = "test-access-token";
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("API connection failed"));

        // Then
        assertThatThrownBy(() -> bankingApiService.getAccounts(accessToken))
                .isInstanceOf(BankingApiException.class)
                .hasMessageContaining("Failed to fetch accounts");
    }
}

