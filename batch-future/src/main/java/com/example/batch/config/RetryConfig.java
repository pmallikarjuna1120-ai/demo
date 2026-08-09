package com.example.batch.config;

import com.example.batch.exception.RetryableCustomerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Configures Spring Retry with exponential backoff for transient failures.
 * Retryable exceptions: network timeouts, connection errors, custom transient errors.
 * Non-retryable exceptions: business validation, data integrity errors.
 */
@Configuration
@EnableRetry
public class RetryConfig {

    @Bean
    public RetryTemplate customerRetryTemplate(BatchProperties properties) {
        RetryTemplate template = new RetryTemplate();
        
        // Define which exceptions trigger retries (transient failures only)
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(RetryableCustomerException.class, true);
        retryableExceptions.put(java.net.SocketTimeoutException.class, true);
        retryableExceptions.put(java.net.ConnectException.class, true);
        
        // Retry policy: max attempts from properties for retryable exceptions
        // Other exceptions fail immediately
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
            properties.getRetryLimit(),  // maxAttempts from config
            retryableExceptions          // exceptions to retry map
        );
        template.setRetryPolicy(retryPolicy);
        
        // Backoff policy: exponential backoff
        // Initial: 100ms, Multiplier: 2.0, Max: 1000ms
        ExponentialBackOffPolicy backoffPolicy = new ExponentialBackOffPolicy();
        backoffPolicy.setInitialInterval(100);      // Start with 100ms
        backoffPolicy.setMultiplier(2.0);           // Double each time
        backoffPolicy.setMaxInterval(1000);         // Cap at 1 second
        template.setBackOffPolicy(backoffPolicy);
        
        return template;
    }
}

