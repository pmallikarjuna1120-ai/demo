package com.example.batch.config;

import com.example.batch.exception.RetryableCustomerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Map;

@Configuration
public class RetryConfig {

    @Bean
    RetryTemplate customerRetryTemplate(BatchProperties properties) {
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(new SimpleRetryPolicy(
                properties.getRetryLimit(),
                Map.of(RetryableCustomerException.class, true),
                true));
        return template;
    }
}
