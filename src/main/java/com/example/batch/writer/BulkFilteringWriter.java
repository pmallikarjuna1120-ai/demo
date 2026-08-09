package com.example.batch.writer;


import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.stereotype.Component;

import com.example.batch.dto.ProcessResult;
import com.example.batch.entity.Customer;
import com.example.batch.listener.ProductionSkipListener;

@Component
public class BulkFilteringWriter implements ItemWriter<ProcessResult> {

    private final JdbcBatchItemWriter<Customer> delegateJdbcWriter;
    private final ProductionSkipListener productionSkipListener;

    public BulkFilteringWriter(JdbcBatchItemWriter<Customer> delegateJdbcWriter, 
    		ProductionSkipListener productionSkipListener) {
        this.delegateJdbcWriter = delegateJdbcWriter;
        this.productionSkipListener = productionSkipListener;
    }

    @Override
    public void write(Chunk<? extends ProcessResult> chunk) throws Exception {
        List<Customer> validCustomers = new ArrayList<>();

        for (ProcessResult result : chunk.getItems()) {
            if (result.isFailed()) {
                // Manually trigger your skip logging and micrometer metrics inside the chunk
            	 validCustomers.add(result.getOriginalInput());
                productionSkipListener.onSkipInProcess(result.getOriginalInput(), result.getException());
            } else {
                validCustomers.add(result.getCustomer());
            }
        }

        // Execute the fast bulk DB insertion query only for the valid items collected
        if (!validCustomers.isEmpty()) {
            delegateJdbcWriter.write(new Chunk<>(validCustomers));
        }
    }
}