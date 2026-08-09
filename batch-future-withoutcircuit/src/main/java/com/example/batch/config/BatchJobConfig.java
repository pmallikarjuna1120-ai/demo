package com.example.batch.config;

import com.example.batch.dto.ProcessResult;
import com.example.batch.entity.Customer;
import com.example.batch.listener.BatchChunkListener;
import com.example.batch.listener.BatchJobListener;
import com.example.batch.listener.BatchSkipListener;
import com.example.batch.listener.BatchStepListener;
import com.example.batch.processor.ParallelItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.Future;

@Configuration
public class BatchJobConfig {

    @Bean
    Job customerJob(
            JobRepository jobRepository,
            Step customerStep,
            BatchJobListener jobListener) {

        return new JobBuilder("customerJob", jobRepository)
                .listener(jobListener)
                .start(customerStep)
                .build();
    }

    @Bean
    Step customerStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Customer> customerReader,
            ParallelItemProcessor<Customer, ProcessResult> parallelCustomerProcessor,
            AsyncItemWriter<ProcessResult> customerWriter,
            BatchStepListener stepListener,
            BatchChunkListener chunkListener,
            BatchSkipListener skipListener,
            BatchProperties properties) {

        return new StepBuilder("customerStep", jobRepository)
                .<Customer, Future<ProcessResult>>chunk(
                        properties.getChunkSize(),
                        transactionManager)
                .reader(customerReader)
                .processor(parallelCustomerProcessor)
                .writer(customerWriter)

                // Explicit listener registration.
                .listener(stepListener)
                .listener(chunkListener)

                /*
                 * IMPORTANT:
                 * Native SkipListener callbacks are only invoked by Spring Batch
                 * when the step itself performs skip/retry processing.
                 *
                 * With an asynchronous processor returning Future, processor
                 * exceptions are represented asynchronously. Therefore this
                 * listener is registered for native read/write skip events, while
                 * ProcessResult failures are handled by the result/audit path.
                 */
                .listener(skipListener)

                .build();
    }
}
