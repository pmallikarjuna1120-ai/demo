package com.example.batch;

import com.example.batch.entity.Customer;
import com.example.batch.processor.ParallelItemProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

class ParallelItemProcessorTest {

    @Test
    void processesItemsInParallelAndReturnsFuturesInSubmissionOrder() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        try {
            ItemProcessor<Customer, Customer> delegate = customer -> {
                Thread.sleep(50);
                customer.setStatus("PROCESSED");
                return customer;
            };

            ParallelItemProcessor<Customer, Customer> processor =
                    new ParallelItemProcessor<>(delegate, executor);

            long start = System.nanoTime();

            List<Future<Customer>> futures = processor.submitAll(List.of(
                    new Customer(1L, "A", "NEW"),
                    new Customer(2L, "B", "NEW"),
                    new Customer(3L, "C", "NEW"),
                    new Customer(4L, "D", "NEW")
            ));

            List<Customer> results = futures.stream()
                    .map(thisFuture -> {
                        try {
                            return thisFuture.get(2, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();

            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

            assertThat(results).hasSize(4);
            assertThat(results).extracting(Customer::getId)
                    .containsExactly(1L, 2L, 3L, 4L);
            assertThat(results).extracting(Customer::getStatus)
                    .containsOnly("PROCESSED");

            // Four 50ms tasks on four workers should be substantially below
            // sequential execution (~200ms), allowing CI scheduling variance.
            assertThat(elapsedMillis).isLessThan(180);
        } finally {
            executor.shutdownNow();
        }
    }
}
