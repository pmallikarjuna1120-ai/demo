package com.example.batch;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@SpringBatchTest
@ActiveProfiles("test") // Activates application-test.properties
@Import(BatchTestConfig.class) // Imports your standalone configuration file
class BulkCustomerJobIT {

	@Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private Job productionAsyncJob; 

    @BeforeEach
    void setUp() {
        // 2. Explicitly assign the job to the test utilities utility
        this.jobLauncherTestUtils.setJob(productionAsyncJob);
    }
    @AfterEach
    void cleanUp() {
        // Wipes framework execution rows cleanly between tests
        jobRepositoryTestUtils.removeJobExecutions();
        // Wipes business rows cleanly between tests
        jdbcTemplate.execute("TRUNCATE TABLE CUSTOMER");
    }
    @Test
    void testLaunchJobSuccessfully() throws Exception {
    	
    	 for(int i=1; i <=2000;i++) {
    		 jdbcTemplate.update("INSERT INTO CUSTOMER(ID,NAME,STATUS) VALUES(?,?,?)",i,"CUSTOMER-"+i,"NEW");
    	 }
        // Given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("run.id", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();

        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
        
        Integer completed = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM CUSTOMER WHERE STATUS='COMPLETED'", Integer.class);
        Assertions.assertEquals(completed, 1998);
    }

    
}