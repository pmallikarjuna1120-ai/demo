package com.example.batch.processor;

import com.example.batch.dto.ProcessResult;
import com.example.batch.entity.Customer;
import com.example.batch.exception.BusinessValidationException;
import com.example.batch.exception.RetryableCustomerException;
import com.example.batch.service.CustomerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

/**
 * Production-grade processor with built-in retry support.
 * 
 * Features:
 * - Transient failures (network, timeout) trigger retries
 * - Business errors fail immediately (no retry)
 * - Metrics tracked: processing time, retry count
 * - Errors captured as data (not thrown), preventing chunk rollback
 */
@Component
public class CustomerProcessor implements ItemProcessor<Customer, ProcessResult> {
    
    private static final Logger log = LoggerFactory.getLogger(CustomerProcessor.class);
    
    private final CustomerService customerService;
    private final RetryTemplate retryTemplate;

    public CustomerProcessor(
        CustomerService customerService,
        RetryTemplate retryTemplate) {
        this.customerService = customerService;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public ProcessResult process(Customer item) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Execute with retry policy for transient failures
            Customer processed = retryTemplate.execute(context -> {
                int attemptNumber = context.getRetryCount() + 1;
                if (attemptNumber > 1) {
                    log.debug(
                        "Retry attempt {} for customer {}", 
                        attemptNumber, 
                        item.getId());
                }
                return customerService.process(item);
            });
            
            long processingTime = System.currentTimeMillis() - startTime;
            log.debug("Customer {} processed successfully in {}ms", 
                item.getId(), processingTime);
            
            return ProcessResult.success(processed, processingTime, 0);
            
        } catch (RetryableCustomerException ex) {
            // Transient error - retries exhausted
            long processingTime = System.currentTimeMillis() - startTime;
            log.warn(
                "Customer {} failed after retries ({} total time): {}", 
                item.getId(), 
                processingTime,
                ex.getMessage());
            item.setStatus("RETRYABLE_FAILED");
            return ProcessResult.failure(item, ex, processingTime, retryTemplate.getRetryPolicy().getMaxAttempts());
            
        } catch (BusinessValidationException ex) {
            // Business error - no retry
            long processingTime = System.currentTimeMillis() - startTime;
            log.warn(
                "Customer {} business validation failed ({}ms): {}", 
                item.getId(), 
                processingTime,
                ex.getMessage());
            item.setStatus("VALIDATION_FAILED");
            return ProcessResult.failure(item, ex, processingTime, 0);
            
        } catch (Exception ex) {
            // Unexpected error
            long processingTime = System.currentTimeMillis() - startTime;
            log.error(
                "Unexpected error processing customer {} ({}ms)", 
                item.getId(),
                processingTime,
                ex);
            item.setStatus("ERROR");
            return ProcessResult.failure(item, ex, processingTime, 0);
        }
    }
}
