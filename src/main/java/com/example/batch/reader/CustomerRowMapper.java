package com.example.batch.reader;

import com.example.batch.entity.Customer;
import java.sql.*;
import org.springframework.jdbc.core.RowMapper;

public class CustomerRowMapper implements RowMapper<Customer> {
	public Customer mapRow(ResultSet rs, int i) throws SQLException {
		Customer c = new Customer();
		c.setId(rs.getLong("ID"));
		c.setName(rs.getString("NAME"));
		c.setStatus(rs.getString("STATUS"));
		return c;
	}
}