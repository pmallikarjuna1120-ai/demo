package com.example.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

import com.example.batch.entity.Customer;

@Component
public class BatchSkipListener implements SkipListener<Customer, Customer> {

    private static final Logger log = LoggerFactory.getLogger(BatchSkipListener.class);

    @Override
    public void onSkipInRead(Throwable t) {
        log.error("SKIP IN READ error={}", t.getMessage(), t);
    }

    @Override
    public void onSkipInWrite(Customer item, Throwable t) {
        log.error("SKIP IN WRITE customerId={} error={}",
                item != null ? item.getId() : null,
                t.getMessage(), t);
    }

    @Override
    public void onSkipInProcess(Customer item, Throwable t) {
        log.warn("SKIP IN PROCESS customerId={} error={}",
                item != null ? item.getId() : null,
                t.getMessage(), t);
    }
}
