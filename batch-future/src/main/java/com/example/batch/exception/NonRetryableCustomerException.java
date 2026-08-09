package com.example.batch.exception;

public class NonRetryableCustomerException extends Exception {
    public NonRetryableCustomerException(String message) {
        super(message);
    }
}
