package com.example.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class ProductionStepListener implements StepExecutionListener {
    private static final Logger log = LoggerFactory.getLogger(ProductionStepListener.class);

    @Override
    public void beforeStep(StepExecution s) {
        log.info("Step started: stepId={}, stepName={}", s.getId(), s.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution s) {
        log.info("Step finished: stepName={}, status={}, read={}, write={}, filter={}, skip={}, rollback={}",
                s.getStepName(), s.getStatus(), s.getReadCount(), s.getWriteCount(),
                s.getFilterCount(), s.getSkipCount(), s.getRollbackCount());
        return s.getExitStatus();
    }
}
