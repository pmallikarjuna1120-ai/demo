package com.example.batch.config;

import java.util.concurrent.ExecutorService;

import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.example.batch.dto.ProcessResult;
import com.example.batch.entity.Customer;
import com.example.batch.processor.CustomerProcessor;
import com.example.batch.processor.ParallelItemProcessor;
import com.example.batch.writer.BulkFilteringWriter;

@Configuration
public class AsyncProcessorConfig {

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

    @Bean
    ExecutorService customerProcessorExecutorService(
            ThreadPoolTaskExecutor customerProcessorExecutor) {
        return customerProcessorExecutor.getThreadPoolExecutor();
    }

    @Bean
    ParallelItemProcessor<Customer, ProcessResult> parallelCustomerProcessor(
            CustomerProcessor delegate,
            ExecutorService customerProcessorExecutorService) {
        return new ParallelItemProcessor<>(
                delegate,
                customerProcessorExecutorService);
    }

    @Bean
    AsyncItemWriter<ProcessResult> customerWriter(BulkFilteringWriter delegate) {
        AsyncItemWriter<ProcessResult> writer = new AsyncItemWriter<>();
        writer.setDelegate(delegate);
        return writer;
    }
}
