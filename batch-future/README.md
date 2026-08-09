# Single Reader -> Multi-thread Processor -> Single Writer

Architecture:

Oracle
  |
  v
ONE JdbcPagingItemReader
  |
  v
Spring Batch chunk
  |
  v
AsyncItemProcessor
  |-- worker-1
  |-- worker-2
  |-- worker-N
  |
  v
ONE AsyncItemWriter
  |
  v
Bulk JDBC update
  |
  v
Oracle

## Retry and skip design

Retry is deliberately implemented inside the worker processor with RetryTemplate.
Only explicitly retryable exceptions are retried.

After retry exhaustion or a non-retryable business error, the processor returns
ProcessResult.failure(). The single writer audits the failed item and excludes it
from the bulk update.

This is important: standard Spring Batch skip/retry DSL is not a reliable way to
model arbitrary per-item Future failures from AsyncItemProcessor. If native
Spring Batch skip/retry semantics are mandatory, prefer a synchronous processor
or partitioned worker steps.

## Counters

readCount   = items read
writeCount  = successful items sent to writer/batch update
skipCount   = audited failed items
commitCount = successful chunk transactions

These counts are not expected to be equal.

## Production rules

1. Processor must be stateless/thread-safe.
2. Do not share mutable collections or database sessions across processor threads.
3. Keep database writes in the single writer.
4. Use a bounded executor queue for back-pressure.
5. Size Hikari pool for the actual downstream concurrency.
6. Make chunk/page/thread counts configurable.
7. Add idempotency/business-key rules for restart/rerun safety.
8. For very large data sets, benchmark partitioning before increasing thread count.

## Configuration

batch:
  chunk-size: 500
  page-size: 500
  worker-threads: 8
  queue-capacity: 1000
  retry-limit: 3
  skip-limit: 100


## Explicit ParallelItemProcessor

`ParallelItemProcessor<I,O>` is the custom component implementing:

ONE ItemReader
  -> N processor worker threads
  -> ONE ItemWriter

It delegates each item to a bounded `ExecutorService` and returns a `Future<O>`.
The Spring Batch chunk step remains responsible for chunk lifecycle and the
single writer invocation. Processor worker threads must remain stateless and
must not perform independent database writes/commits.
