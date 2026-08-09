package com.example.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class BatchStepListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(BatchStepListener.class);

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("STEP START id={} name={}",
                stepExecution.getId(), stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info(
                "STEP END id={} status={} read={} write={} filter={} skip={} commit={} rollback={}",
                stepExecution.getId(),
                stepExecution.getStatus(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getFilterCount(),
                stepExecution.getSkipCount(),
                stepExecution.getCommitCount(),
                stepExecution.getRollbackCount());

        return stepExecution.getExitStatus();
    }
}
