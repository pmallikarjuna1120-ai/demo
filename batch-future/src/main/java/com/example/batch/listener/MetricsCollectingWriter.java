package com.example.batch.listener;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import com.example.batch.dto.ProcessResult;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

/**
 * Metrics-collecting wrapper for batch writing.
 * 
 * Tracks:
 * - batch.items.processed{status="success"} counter
 * - batch.items.processed{status="failed"} counter
 * - batch.errors{type="ExceptionType"} counter per exception
 * - batch.processing.time timer
 * - batch.retries counter
 * - Logs failure distribution per chunk
 */
public class MetricsCollectingWriter implements ItemWriter<ProcessResult> {
    
    private static final Logger log = LoggerFactory.getLogger(MetricsCollectingWriter.class);
    
    private final MeterRegistry meterRegistry;
    private final ItemWriter<ProcessResult> delegate;

    public MetricsCollectingWriter(
        MeterRegistry meterRegistry,
        ItemWriter<ProcessResult> delegate) {
        this.meterRegistry = meterRegistry;
        this.delegate = delegate;
    }

    @Override
    public void write(Chunk<? extends ProcessResult> chunk) throws Exception {
        Map<String, Integer> errorTypeCount = new HashMap<>();
        long totalProcessingTime = 0;
        int successCount = 0;
        int failureCount = 0;
        int totalRetries = 0;

        // Analyze chunk
        for (ProcessResult result : chunk.getItems()) {
            totalProcessingTime += result.processingTimeMs();
            totalRetries += result.retryCount();
            
            if (result.succeeded()) {
                successCount++;
            } else {
                failureCount++;
                String errorType = result.getErrorType();
                errorTypeCount.merge(errorType, 1, Integer::sum);
            }
        }

        // Record success/failure counts
        if (successCount > 0) {
            meterRegistry.counter(
                "batch.items.processed",
                Tags.of("status", "success"))
                .increment(successCount);
        }
        
        if (failureCount > 0) {
            meterRegistry.counter(
                "batch.items.processed",
                Tags.of("status", "failed"))
                .increment(failureCount);
        }
        
        // Record processing time distribution
        if (totalProcessingTime > 0) {
            meterRegistry.timer("batch.processing.time")
                .record(totalProcessingTime, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
        
        // Record retry count
        if (totalRetries > 0) {
            meterRegistry.counter("batch.retries")
                .increment(totalRetries);
        }
        
        // Record failure breakdown by type
        if (!errorTypeCount.isEmpty()) {
            for (Map.Entry<String, Integer> entry : errorTypeCount.entrySet()) {
                meterRegistry.counter(
                    "batch.errors",
                    Tags.of("type", entry.getKey()))
                    .increment(entry.getValue());
            }
            
            // Log summary
            log.info(
                "Chunk processed: {} success, {} failed ({}). Error types: {}",
                successCount,
                failureCount,
                totalProcessingTime,
                errorTypeCount);
        } else if (successCount > 0) {
            log.info(
                "Chunk processed: {} success in {}ms",
                successCount,
                totalProcessingTime);
        }

        // Delegate to actual writer (BulkFilteringWriter)
        delegate.write(chunk);
    }
}
