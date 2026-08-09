package com.example.batch.writer;

import com.example.batch.dto.ProcessResult;
import com.example.batch.entity.Customer;
import com.example.batch.listener.AsyncProcessResultListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BulkFilteringWriter implements ItemWriter<ProcessResult> {

    private static final Logger log = LoggerFactory.getLogger(BulkFilteringWriter.class);

    private final JdbcTemplate jdbcTemplate;
    private final AsyncProcessResultListener resultListener;

    public BulkFilteringWriter(
            JdbcTemplate jdbcTemplate,
            AsyncProcessResultListener resultListener) {
        this.jdbcTemplate = jdbcTemplate;
        this.resultListener = resultListener;
    }

    @Override
    public void write(Chunk<? extends ProcessResult> chunk) {

        List<Customer> successful = new ArrayList<>();

        for (ProcessResult result : chunk.getItems()) {
            if (result == null || result.error() != null) {
                if (result != null) {
                    resultListener.onFailure(result);
                }
                continue;
            }

            successful.add(result.customer());
        }

        if (successful.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
                "UPDATE CUSTOMER SET STATUS = ? WHERE ID = ?",
                successful,
                successful.size(),
                (ps, customer) -> {
                    ps.setString(1, customer.getStatus());
                    ps.setLong(2, customer.getId());
                });

        log.debug("Writer persisted {} customers", successful.size());
    }
}
