package com.example.batch.config;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.batch.dto.ProcessResult;
import com.example.batch.entity.Customer;
import com.example.batch.listener.ProductionChunkListener;
import com.example.batch.listener.ProductionJobListener;
import com.example.batch.listener.ProductionSkipListener;
import com.example.batch.listener.ProductionStepListener;
import com.example.batch.processor.CustomerProcessor;
import com.example.batch.service.CustomerService;
import com.example.batch.writer.BulkFilteringWriter;

@Configuration
@EnableConfigurationProperties(BatchProperties.class)
public class ProductionAsyncBatchConfig {


    private final BatchProperties batchProperties;
    // Spring Boot automatically injects the active Prometheus MeterRegistry
    public ProductionAsyncBatchConfig(BatchProperties batchProperties) {
        this.batchProperties = batchProperties;
        System.out.println("🚀 Active Chunk Size Configured: " + this.batchProperties.getChunkSize()); 
    }

    /**
     * 1. Core TaskExecutor tuned to prevent OutOfMemoryErrors.
     */
    @Bean
    TaskExecutor asyncBatchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(this.batchProperties.getWorkerThreads());
        executor.setMaxPoolSize(this.batchProperties.getWorkerThreads() * 2);
        executor.setQueueCapacity(this.batchProperties.getQueueCapacity());
        executor.setThreadNamePrefix("async-batch-worker-");
        
        // Critical: Throttles input readers when processing queue fills up.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }
    
    @Bean
	CustomerProcessor customerProcessor(CustomerService s) {
		return new CustomerProcessor(s);
	}
    
    @Bean
	AsyncItemProcessor<Customer, ProcessResult> asyncItemProcessor(CustomerProcessor p, TaskExecutor asyncBatchTaskExecutor) {
		AsyncItemProcessor<Customer, ProcessResult> a = new AsyncItemProcessor<>();
		a.setDelegate(p);
		a.setTaskExecutor(asyncBatchTaskExecutor);
		return a;
	}
    @Bean
    JdbcBatchItemWriter<Customer> bulkCustomerJdbcWriter(DataSource ds){
      return new JdbcBatchItemWriterBuilder<Customer>()
         .dataSource(ds)
         .sql("update CUSTOMER set STATUS=:status where ID=:id")
         .beanMapped()
         .assertUpdates(false) 
         .build();
    }
    /**
     * 1. Define your Custom Filtering Writer as a managed Spring Bean.
     */
    @Bean
    BulkFilteringWriter bulkFilteringWriter(
            JdbcBatchItemWriter<Customer> bulkCustomerJdbcWriter,
            ProductionSkipListener productionSkipListener) {
        return new BulkFilteringWriter(bulkCustomerJdbcWriter, productionSkipListener);
    }

    /**
     * 2. Wrap the BulkFilteringWriter inside the AsyncItemWriter wrapper.
     * This satisfies the missing bean dependency.
     */
    @Bean
    AsyncItemWriter<ProcessResult> asyncBulkItemWriter(BulkFilteringWriter bulkFilteringWriter) {
        AsyncItemWriter<ProcessResult> asyncWriter = new AsyncItemWriter<>();
        
        // Wire the custom result filter as the delegate target
        asyncWriter.setDelegate(bulkFilteringWriter); 
        
        return asyncWriter;
    }

    /**
     * 3. Multi-Threaded Fault Tolerant Step Definition.
     * @param productionSkipListener 
     */
    @Bean
    public Step productionAsyncStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   ItemReader<Customer> customerReader,
                                   AsyncItemProcessor<Customer, ProcessResult> productionAsyncProcessor,
                                   AsyncItemWriter<ProcessResult> asyncBulkItemWriter,
                                   ProductionChunkListener chunkLogger,
                                   ProductionStepListener stepExecutionLogger, ProductionSkipListener productionSkipListener) {
        return new StepBuilder("productionAsyncStep", jobRepository)
                .<Customer, java.util.concurrent.Future<ProcessResult>>chunk(this.batchProperties.getChunkSize(), transactionManager)
                .reader(customerReader)
                .processor(productionAsyncProcessor)
                .writer(asyncBulkItemWriter)
                
                // Configure Fault Tolerance & Skip Rules
                .faultTolerant()
                // 2. STOP automatic retries on async worker exceptions
                // This prevents the ExhaustedRetryException crash
                .noRetry(ExecutionException.class)
                
                // 3. STOP transactions from rolling back instantly on async exceptions
                .noRollback(ExecutionException.class)
                
                // 4. Register ExecutionException as a skippable exception framework type
                .skip(ExecutionException.class)
                .skipLimit(100)               
               
                .listener(productionSkipListener) // Binds skip logging and micrometer metrics
                .listener(chunkLogger)
                .listener(stepExecutionLogger)
                
                .build();
    }
	
	@Bean
    public Job productionAsyncJob(JobRepository jobRepository, Step productionAsyncStep,ProductionJobListener productionJobListener) {
        return new JobBuilder("productionAsyncJob", jobRepository)
        		.listener(productionJobListener)
                .start(productionAsyncStep)
                .build();
    }
}