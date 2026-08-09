package com.example.batch.dto;

import com.example.batch.entity.Customer;
import java.time.Instant;

/**
 * Enhanced ProcessResult with optional metadata tracking.
 * Supports both simple (success/failure) and detailed (with metrics) usage.
 * 
 * Example:
 *   // Simple usage (backward compatible)
 *   ProcessResult.success(customer)
 *   ProcessResult.failure(customer, exception)
 *   
 *   // With metrics
 *   ProcessResult.success(customer, 125L, 0)  // 125ms, 0 retries
 *   ProcessResult.failure(customer, ex, 500L, 2)  // 500ms, 2 retries
 */
public record ProcessResult(
    Customer customer,           // Null if failed
    Customer originalInput,      // Always present
    Throwable error,             // Null if success
    long processingTimeMs,       // Processing duration
    int retryCount,              // Actual retry attempts
    Instant processedAt) {       // Timestamp

    // ============= SUCCESS FACTORY METHODS =============
    
    /**
     * Simple success (backward compatible, no metrics).
     * @param customer The processed customer object
     */
    public static ProcessResult success(Customer customer) {
        return new ProcessResult(
            customer,
            customer,
            null,
            0L,      // no tracking
            0,       // no retries
            Instant.now());
    }

    /**
     * Success with metrics (processing time and retry count).
     * @param customer The processed customer object
     * @param processingTimeMs How long processing took
     * @param retryCount How many retries were attempted
     */
    public static ProcessResult success(
        Customer customer,
        long processingTimeMs,
        int retryCount) {
        return new ProcessResult(
            customer,
            customer,
            null,
            processingTimeMs,
            retryCount,
            Instant.now());
    }

    // ============= FAILURE FACTORY METHODS =============
    
    /**
     * Simple failure (backward compatible, no metrics).
     * @param originalInput The original customer that failed
     * @param error The exception that occurred
     */
    public static ProcessResult failure(
        Customer originalInput,
        Throwable error) {
        return new ProcessResult(
            null,
            originalInput,
            error,
            0L,      // no tracking
            0,       // no retries
            Instant.now());
    }

    /**
     * Failure with metrics (processing time and retry count).
     * @param originalInput The original customer that failed
     * @param error The exception that occurred
     * @param processingTimeMs How long processing took before failing
     * @param retryCount How many retries were attempted
     */
    public static ProcessResult failure(
        Customer originalInput,
        Throwable error,
        long processingTimeMs,
        int retryCount) {
        return new ProcessResult(
            null,
            originalInput,
            error,
            processingTimeMs,
            retryCount,
            Instant.now());
    }

    // ============= CONVENIENCE METHODS =============
    
    /**
     * Check if this result represents a failure.
     */
    public boolean failed() {
        return error != null;
    }

    /**
     * Check if this result represents a success.
     */
    public boolean succeeded() {
        return error == null && customer != null;
    }

    /**
     * Get the exception class name for logging/metrics.
     */
    public String getErrorType() {
        if (error == null) return "SUCCESS";
        return error.getClass().getSimpleName();
    }

    /**
     * Get error message for logging.
     */
    public String getErrorMessage() {
        if (error == null) return null;
        return error.getMessage();
    }

    /**
     * Get customer ID regardless of success/failure.
     */
    public Long getCustomerId() {
        Customer customer = this.customer != null ? this.customer : this.originalInput;
        return customer != null ? customer.getId() : null;
    }
}

