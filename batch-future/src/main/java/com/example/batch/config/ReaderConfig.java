package com.example.batch.config;

import com.example.batch.entity.Customer;
import com.example.batch.reader.CustomerRowMapper;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class ReaderConfig {

    @Bean
    JdbcPagingItemReader<Customer> customerReader(
            DataSource dataSource,
            BatchProperties properties) {

        return new JdbcPagingItemReaderBuilder<Customer>()
                .name("customerReader")
                .dataSource(dataSource)
                .selectClause("SELECT ID, NAME, STATUS")
                .fromClause("FROM CUSTOMER")
                .whereClause("WHERE ID > :minId")
                .parameterValues(Map.of("minId", 0L))
                .sortKeys(Map.of("ID", Order.ASCENDING))
                .pageSize(properties.getPageSize())
                .fetchSize(properties.getPageSize())
                .rowMapper(new CustomerRowMapper())
                .saveState(true)
                .build();
    }
}
