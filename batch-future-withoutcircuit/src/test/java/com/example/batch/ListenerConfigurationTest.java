package com.example.batch;

import com.example.batch.config.BatchJobConfig;
import com.example.batch.listener.BatchChunkListener;
import com.example.batch.listener.BatchJobListener;
import com.example.batch.listener.BatchSkipListener;
import com.example.batch.listener.BatchStepListener;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ListenerConfigurationTest {

    @Autowired Job customerJob;
    @Autowired Step customerStep;
    @Autowired BatchJobListener jobListener;
    @Autowired BatchStepListener stepListener;
    @Autowired BatchChunkListener chunkListener;
    @Autowired BatchSkipListener skipListener;

    @Test
    void listenerBeansAreConfigured() {
        assertThat(customerJob).isNotNull();
        assertThat(customerStep).isNotNull();
        assertThat(jobListener).isNotNull();
        assertThat(stepListener).isNotNull();
        assertThat(chunkListener).isNotNull();
        assertThat(skipListener).isNotNull();
    }
}
