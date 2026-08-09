package com.example.batch.dto;

import com.example.batch.entity.Customer;

public record ProcessResult(
        Customer customer,
        Customer originalInput,
        Throwable error) {

    public static ProcessResult success(Customer customer) {
        return new ProcessResult(customer, customer, null);
    }

    public static ProcessResult failure(Customer originalInput, Throwable error) {
        return new ProcessResult(null, originalInput, error);
    }

    public boolean failed() {
        return error != null;
    }
}
