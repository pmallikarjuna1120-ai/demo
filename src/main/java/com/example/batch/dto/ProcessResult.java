package com.example.batch.dto;

import com.example.batch.entity.Customer;

public class ProcessResult {
    private final Customer customer;
    private final Throwable exception;
    private final Customer originalInput;

    public ProcessResult(Customer customer, Customer originalInput) {
        this.customer = customer;
        this.originalInput = originalInput;
        this.exception = null;
    }

    public ProcessResult(Throwable exception, Customer originalInput) {
        this.customer = null;
        this.originalInput = originalInput;
        this.exception = exception;
    }

    public boolean isFailed() { return exception != null; }
    public Customer getCustomer() { return customer; }
    public Throwable getException() { return exception; }
    public Customer getOriginalInput() { return originalInput; }
}