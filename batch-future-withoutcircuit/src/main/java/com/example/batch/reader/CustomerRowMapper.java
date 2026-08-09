package com.example.batch.reader;

import com.example.batch.entity.Customer;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerRowMapper implements RowMapper<Customer> {
    @Override
    public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Customer(
                rs.getLong("ID"),
                rs.getString("NAME"),
                rs.getString("STATUS"));
    }
}
