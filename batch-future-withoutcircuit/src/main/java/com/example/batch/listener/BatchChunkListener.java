package com.example.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

@Component
public class BatchChunkListener implements ChunkListener {

    private static final Logger log = LoggerFactory.getLogger(BatchChunkListener.class);

    @Override
    public void beforeChunk(ChunkContext context) {
        log.debug("CHUNK START step={}",
                context.getStepContext().getStepName());
    }

    @Override
    public void afterChunk(ChunkContext context) {
        log.debug("CHUNK COMMIT step={}",
                context.getStepContext().getStepName());
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        log.error("CHUNK ERROR / ROLLBACK step={}",
                context.getStepContext().getStepName());
    }
}
