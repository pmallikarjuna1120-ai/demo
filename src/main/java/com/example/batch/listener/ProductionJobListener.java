package com.example.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class ProductionJobListener implements JobExecutionListener {
    private static final Logger log = LoggerFactory.getLogger(ProductionJobListener.class);

    @Override
    public void beforeJob(JobExecution execution) {
        log.info("Batch job started: jobId={}, jobName={}, parameters={}",
                execution.getId(), execution.getJobInstance().getJobName(),
                execution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution execution) {
        log.info("Batch job finished: jobId={}, status={}, exitCode={}, read={}, write={}, skip={}",
                execution.getId(), execution.getStatus(), execution.getExitStatus().getExitCode(),
                execution.getStepExecutions().stream().mapToLong(s -> s.getReadCount()).sum(),
                execution.getStepExecutions().stream().mapToLong(s -> s.getWriteCount()).sum(),
                execution.getStepExecutions().stream().mapToLong(s -> s.getSkipCount()).sum());
    }
}
