package com.example.batch.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Application-wide configuration for batch processing.
 * - Enables @ConfigurationProperties for BatchProperties
 * - Configures Resilience4j Circuit Breaker for fault tolerance
 */
@Configuration
@EnableConfigurationProperties(BatchProperties.class)
public class BatchApplicationConfig {

    /**
     * Resilience4j Circuit Breaker for customer processor.
     * 
     * State Machine:
     * - CLOSED (normal): Requests pass through
     * - OPEN (failure): Requests fail fast (after 50% failure rate)
     * - HALF_OPEN (recovery): Tests if service recovered
     * 
     * Thresholds:
     * - Failure rate: 50% (open if 50%+ of last 100 calls failed)
     * - Slow call rate: 100% with 2s threshold (open if slow)
     * - Wait duration: 10s (before transitioning to HALF_OPEN)
     * - Half-open attempts: 5 (test 5 calls in recovery mode)
     */
    @Bean
    public CircuitBreaker customerProcessorCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            // Failure detection
            .failureRateThreshold(50.0f)             // 50% failure rate triggers
            .slowCallRateThreshold(100.0f)           // 100% slow calls
            .slowCallDurationThreshold(Duration.ofSeconds(2))  // > 2s = slow
            
            // State transitions
            .waitDurationInOpenState(Duration.ofSeconds(10))   // Wait before half-open
            .permittedNumberOfCallsInHalfOpenState(5)          // Test 5 calls
            .automaticTransitionFromOpenToHalfOpenEnabled(true) // Auto-recover
            
            // Metrics window
            .slidingWindowSize(100)        // Consider last 100 calls
            .minimumNumberOfCalls(20)      // Need at least 20 calls before deciding
            
            .build();
        
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        return registry.circuitBreaker("customerProcessor");
    }
}

