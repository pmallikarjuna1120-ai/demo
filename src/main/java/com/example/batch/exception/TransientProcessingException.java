package com.example.batch.exception;

public class TransientProcessingException extends RuntimeException {
    public TransientProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
    public TransientProcessingException(String message) {
        super(message);
    }
}
