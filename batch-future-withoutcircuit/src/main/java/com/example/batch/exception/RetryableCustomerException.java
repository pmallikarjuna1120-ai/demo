package com.example.batch.exception;

public class RetryableCustomerException extends Exception {
    public RetryableCustomerException(String message) {
        super(message);
    }
}
