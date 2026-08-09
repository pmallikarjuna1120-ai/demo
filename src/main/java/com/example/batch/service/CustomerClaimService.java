package com.example.batch.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CustomerClaimService {
	private final JdbcTemplate jdbcTemplate;

	public CustomerClaimService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public int  claimRecords(int limit) throws InterruptedException {
		
		return jdbcTemplate.update("update CUSTOMER set STATUS='PROCESSING' where ID in (SELECT ID FROM CUSTOMER WHERE STATUS='NEW' ORDERB Y ID FETCH FIRST? ROWS ONLY",limit);
	}
}
