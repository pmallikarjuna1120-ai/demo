package com.example.batch.processor;

import com.example.batch.dto.ProcessResult;
import com.example.batch.entity.Customer;
import com.example.batch.exception.BusinessValidationException;
import com.example.batch.service.CustomerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class CustomerProcessor implements ItemProcessor<Customer, ProcessResult> {
	 private static final Logger log = LoggerFactory.getLogger(CustomerProcessor.class);
	private final CustomerService service;

	public CustomerProcessor(CustomerService service) {
		this.service = service;
	}

	@Override
	public ProcessResult  process(Customer item) throws Exception {
		  try {
		if(item.getId()%1000==0) {
			log.info("Customer:"+item.getName());
			throw new BusinessValidationException("not process");
			
		}
		item = service.process(item);
		  // Success Path
        return new ProcessResult(item, item);
        
    } catch (BusinessValidationException ex) {
        // FIX: Catch it, log it silently, and wrap it cleanly
    	item.setStatus("FAILED");
        return new ProcessResult(ex, item);
    }
	}
}
