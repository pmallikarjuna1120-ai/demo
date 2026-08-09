package com.example.batch;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test") // Activates application-test.properties
@Import(BatchTestConfig.class) // Imports your standalone configuration file
class MyBatchJobIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @AfterEach
    void cleanUp() {
        // Clears the H2 metadata tables between test runs
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    void testLaunchJobSuccessfully() throws Exception {
        // Given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("run.id", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();

        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
    }

    @Test
    void testLaunchSingleStep() {
        // When: Launching only a specific step by its name
        JobExecution stepExecution = jobLauncherTestUtils.launchStep("myCustomStep");

        // Then
        Assertions.assertEquals(ExitStatus.COMPLETED, stepExecution.getExitStatus());
    }
}