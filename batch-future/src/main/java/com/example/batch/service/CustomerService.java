package com.example.batch.service;

import com.example.batch.entity.Customer;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    /*
     * Keep this method stateless/thread-safe.
     * It can contain CPU/business transformation or controlled downstream calls.
     */
    public Customer process(Customer customer) throws Exception {
        if (customer.getName() == null || customer.getName().isBlank()) {
            throw new IllegalArgumentException("Customer name is mandatory");
        }

        // Example transformation.
        customer.setStatus("PROCESSED");
        return customer;
    }
}
