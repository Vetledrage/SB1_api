package com.vetleDemo.store.service;

public class BankingApiException extends RuntimeException {

    public BankingApiException(String message) {
        super(message);
    }

    public BankingApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

