package com.example.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

import com.example.batch.entity.Customer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;

@Component
public class ProductionSkipListener implements SkipListener<Customer, Customer> {
private static final Logger log = LoggerFactory.getLogger(ProductionSkipListener.class);
    
    private final Counter readSkipCounter;
    private final Counter processSkipCounter;
    private final Counter writeSkipCounter;

    // Inject MeterRegistry to register your Prometheus telemetry metrics
    public ProductionSkipListener(MeterRegistry meterRegistry) {
        this.readSkipCounter = Counter.builder("batch.customer.skip.count")
                .tag("phase", "read")
                .description("Total items skipped during the item reading phase")
                .register(meterRegistry);

        this.processSkipCounter = Counter.builder("batch.customer.skip.count")
                .tag("phase", "process")
                .description("Total items skipped during asynchronous heavy processing")
                .register(meterRegistry);

        this.writeSkipCounter = Counter.builder("batch.customer.skip.count")
                .tag("phase", "write")
                .description("Total items skipped during database block persistence writes")
                .register(meterRegistry);
    }

    @Override
    public void onSkipInRead(Throwable t) {
        readSkipCounter.increment();
        log.warn("⚠️ [READ SKIP] Record skipped due to data corruption error: {}", t.getMessage());
    }

	@Override
	public void onSkipInProcess(Customer item, Throwable t) {
		processSkipCounter.increment();

		// Extract the real business validation error from the async wrapper
		Throwable realException = (t instanceof java.util.concurrent.ExecutionException) ? t.getCause() : t;

		log.error("❌ [PROCESS SKIP] Skipped item [{}] -> Reason: {}", item,
				(realException != null ? realException.getMessage() : t.getMessage()));

	}

    @Override
    public void onSkipInWrite(Customer item, Throwable t) {
        writeSkipCounter.increment();
        log.error("💥 [WRITE SKIP] Database target constraint rejection for item [{}] -> Reason: {}", item, t.getMessage());
    }
}
