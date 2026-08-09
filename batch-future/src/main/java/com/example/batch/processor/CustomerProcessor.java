package com.example.batch.processor;

import com.example.batch.dto.ProcessResult;
import com.example.batch.entity.Customer;
import com.example.batch.exception.BusinessValidationException;
import com.example.batch.service.CustomerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class CustomerProcessor implements ItemProcessor<Customer, ProcessResult> {
	private static final Logger log = LoggerFactory.getLogger(CustomerProcessor.class);

    private final CustomerService customerService;

    public CustomerProcessor(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public ProcessResult process(Customer item) throws Exception {
    	log.debug("Customer Name:{}",item.getName());
        try {
            // Example business rule. Replace with real validation.
            if (item.getId() != null && item.getId() % 1000 == 0) {
                throw new BusinessValidationException(
                        "Business validation failed for customer " + item.getId());
            }

            Customer processed = customerService.process(item);
            return ProcessResult.success(processed);

        } catch (BusinessValidationException ex) {
            // Known business errors are represented as data so that the
            // single writer can record/skip them without corrupting the
            // async Future contract.
        	item.setStatus("FAILED");
            return ProcessResult.failure(item, ex);
        }
    }
}
