package com.example.batch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.batch")
public class BatchProperties {
    private int chunkSize = 500;
    private int pageSize = 1000;
    private int workerThreads = 8;
    private int queueCapacity = 1000;
    private int retryLimit = 3;
    private int skipLimit = 100;

    public int getChunkSize(){return chunkSize;}
    public void setChunkSize(int v){chunkSize=v;}
    public int getPageSize(){return pageSize;}
    public void setPageSize(int v){pageSize=v;}
    public int getWorkerThreads(){return workerThreads;}
    public void setWorkerThreads(int v){workerThreads=v;}
    public int getQueueCapacity(){return queueCapacity;}
    public void setQueueCapacity(int v){queueCapacity=v;}
    public int getRetryLimit(){return retryLimit;}
    public void setRetryLimit(int v){retryLimit=v;}
    public int getSkipLimit(){return skipLimit;}
    public void setSkipLimit(int v){skipLimit=v;}
}
