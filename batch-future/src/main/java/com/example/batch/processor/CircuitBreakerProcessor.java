package com.example.batch.processor;

import com.example.batch.dto.ProcessResult;
import com.example.batch.entity.Customer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resilience4j Circuit Breaker wrapper for customer processor.
 * 
 * Prevents cascading failures:
 * - If downstream service fails (50%+ errors), circuit opens
 * - New requests fail fast instead of retrying
 * - After 10 seconds, circuit goes half-open for recovery attempt
 * 
 * Benefits:
 * - Protects downstream services from overload
 * - Faster failure detection and recovery
 * - Integrated with batch processing metrics
 */
@Component
public class CircuitBreakerProcessor implements ItemProcessor<Customer, ProcessResult> {
    
    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerProcessor.class);
    
    private final CircuitBreaker circuitBreaker;
    private final ItemProcessor<Customer, ProcessResult> delegate;

    public CircuitBreakerProcessor(
        CircuitBreaker circuitBreaker,
        ItemProcessor<Customer, ProcessResult> delegate) {
        this.circuitBreaker = circuitBreaker;
        this.delegate = delegate;
    }

    @Override
    public ProcessResult process(Customer item) throws Exception {
        // Execute delegate with circuit breaker protection
        try {
            return circuitBreaker.executeSupplier(() -> {
                try {
                    return delegate.process(item);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            
        } catch (Exception ex) {
            // Circuit breaker open or other CB exception
            log.warn(
                "Circuit breaker triggered for customer {}. State: {}. Reason: {}",
                item.getId(),
                circuitBreaker.getState(),
                ex.getMessage());
            
            item.setStatus("CIRCUIT_BREAKER_OPEN");
            return ProcessResult.failure(item, ex, 0, 0);
        }
    }
}
