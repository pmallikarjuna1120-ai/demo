package com.example.batch.config;

import java.util.concurrent.ExecutorService;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.example.batch.dto.ProcessResult;
import com.example.batch.entity.Customer;
import com.example.batch.listener.MetricsCollectingWriter;
import com.example.batch.processor.CircuitBreakerProcessor;
import com.example.batch.processor.CustomerProcessor;
import com.example.batch.processor.ParallelItemProcessor;
import com.example.batch.writer.BulkFilteringWriter;

/**
 * Async processing configuration combining:
 * - ParallelItemProcessor (N workers via ExecutorService)
 * - CircuitBreakerProcessor (fault tolerance)
 * - CustomerProcessor (retry logic)
 * - MetricsCollectingWriter (observability)
 * - BulkFilteringWriter (database writes)
 * - AsyncItemWriter (Spring Batch wrapper)
 */
@Configuration
public class AsyncProcessorConfig {

    /**
     * Executor for parallel item processing.
     * Creates thread pool with size from BatchProperties.
     * Bounded queue prevents memory overflow.
     * Graceful shutdown waits for in-flight work.
     */
    @Bean
    ThreadPoolTaskExecutor customerProcessorExecutor(BatchProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getWorkerThreads());
        executor.setMaxPoolSize(properties.getWorkerThreads());
        executor.setQueueCapacity(properties.getQueueCapacity());
        executor.setThreadNamePrefix("customer-processor-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * Extracts ExecutorService from ThreadPoolTaskExecutor for ParallelItemProcessor.
     */
    @Bean
    ExecutorService customerProcessorExecutorService(
            ThreadPoolTaskExecutor customerProcessorExecutor) {
        return customerProcessorExecutor.getThreadPoolExecutor();
    }

    /**
     * Wraps CustomerProcessor with Circuit Breaker for fault tolerance.
     */
    @Bean
    ItemProcessor<Customer, ProcessResult> circuitBreakerCustomerProcessor(
            CustomerProcessor delegate,
            CircuitBreaker customerProcessorCircuitBreaker) {
        return new CircuitBreakerProcessor(customerProcessorCircuitBreaker, delegate);
    }

    /**
     * ParallelItemProcessor: distributes items to N worker threads.
     * Each item is submitted to ExecutorService.
     * Returns Future<ProcessResult> for async processing.
     */
    @Bean
    ParallelItemProcessor<Customer, ProcessResult> parallelCustomerProcessor(
            ItemProcessor<Customer, ProcessResult> circuitBreakerCustomerProcessor,
            ExecutorService customerProcessorExecutorService) {
        return new ParallelItemProcessor<>(
                circuitBreakerCustomerProcessor,
                customerProcessorExecutorService);
    }

    /**
     * MetricsCollectingWriter: wraps actual writer with observability.
     * Tracks: success/failure counts, processing time, error types, retry count.
     * Delegates to BulkFilteringWriter for actual database writes.
     */
    @Bean
    ItemWriter<ProcessResult> metricsCollectingWriter(
            BulkFilteringWriter bulkFilteringWriter,
            io.micrometer.core.instrument.MeterRegistry meterRegistry) {
        return new MetricsCollectingWriter(meterRegistry, bulkFilteringWriter);
    }

    /**
     * AsyncItemWriter: Spring Batch wrapper for async processing results.
     * Unwraps Futures and batches results for writing.
     */
    @Bean
    AsyncItemWriter<ProcessResult> customerWriter(
            ItemWriter<ProcessResult> metricsCollectingWriter) {
        AsyncItemWriter<ProcessResult> writer = new AsyncItemWriter<>();
        writer.setDelegate(metricsCollectingWriter);
        return writer;
    }
}
