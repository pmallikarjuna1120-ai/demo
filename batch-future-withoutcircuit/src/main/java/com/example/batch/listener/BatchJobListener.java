package com.example.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class BatchJobListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(BatchJobListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("JOB START id={} name={}",
                jobExecution.getId(),
                jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long read = jobExecution.getStepExecutions()
                .stream().mapToLong(s -> s.getReadCount()).sum();
        long write = jobExecution.getStepExecutions()
                .stream().mapToLong(s -> s.getWriteCount()).sum();
        long skip = jobExecution.getStepExecutions()
                .stream().mapToLong(s -> s.getSkipCount()).sum();
        long filter = jobExecution.getStepExecutions()
                .stream().mapToLong(s -> s.getFilterCount()).sum();

        log.info("JOB END id={} status={} read={} write={} filter={} skip={}",
                jobExecution.getId(),
                jobExecution.getStatus(),
                read, write, filter, skip);
    }
}
