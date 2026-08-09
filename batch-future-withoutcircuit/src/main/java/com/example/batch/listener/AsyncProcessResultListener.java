package com.example.batch.listener;

import com.example.batch.dto.ProcessResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AsyncProcessResultListener {

    private static final Logger log =
            LoggerFactory.getLogger(AsyncProcessResultListener.class);

    public void onFailure(ProcessResult result) {
        if (result != null && result.error() != null) {
            log.warn("ASYNC PROCESS SKIP itemId={} error={}",
                    result.originalInput() != null ? result.originalInput().getId() : null,
                    result.error().getMessage(),
                    result.error());
        }
    }
}
