package com.example.batch.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

/**
 * Executes item processing in parallel while preserving the input order
 * of the returned Future objects.
 *
 * This component is useful when the requirement is:
 *
 *   ONE Reader -> N Processor Threads -> ONE Writer
 *
 * The Spring Batch step still owns the chunk transaction. Worker threads
 * must not perform independent database writes or transaction commits.
 */
public class ParallelItemProcessor<I, O> implements ItemProcessor<I, Future<O>> {

    private final ItemProcessor<I, O> delegate;
    private final ExecutorService executor;

    public ParallelItemProcessor(ItemProcessor<I, O> delegate,
                                 ExecutorService executor) {
        this.delegate = delegate;
        this.executor = executor;
    }

    @Override
    public Future<O> process(I item) {
        Callable<O> task = () -> delegate.process(item);
        return executor.submit(task);
    }

    /**
     * Optional helper for callers that already have a chunk and want to
     * submit all items together.
     */
    public List<Future<O>> submitAll(List<? extends I> items) {
        List<Future<O>> futures = new ArrayList<>(items.size());
        for (I item : items) {
            futures.add(process(item));
        }
        return futures;
    }
}
