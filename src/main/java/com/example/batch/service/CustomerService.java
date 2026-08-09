package com.example.batch.service;
import com.example.batch.entity.Customer;
import org.springframework.stereotype.Service;
@Service
public class CustomerService{
 public Customer process(Customer c) throws InterruptedException{
   Thread.sleep(100);
   c.setStatus("COMPLETED");
   return c;
 }
}
