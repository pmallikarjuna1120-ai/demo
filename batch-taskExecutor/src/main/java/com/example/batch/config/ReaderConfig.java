package com.example.batch.config;

import javax.sql.DataSource;
import com.example.batch.entity.Customer;
import com.example.batch.reader.CustomerRowMapper;
import org.springframework.context.annotation.*;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.OraclePagingQueryProvider;
import org.springframework.batch.item.database.Order;
import java.util.Map;

@Configuration
public class ReaderConfig {
	@Bean
	OraclePagingQueryProvider queryProvider() {
		OraclePagingQueryProvider p = new OraclePagingQueryProvider();
		p.setSelectClause("SELECT ID,NAME,STATUS");
		p.setFromClause("FROM CUSTOMER");
		p.setWhereClause("STATUS='NEW'");
		p.setSortKeys(Map.of("ID", Order.ASCENDING));
		return p;
	}

	@Bean
	JdbcPagingItemReader<Customer> reader(DataSource ds, OraclePagingQueryProvider qp) {
		JdbcPagingItemReader<Customer> r = new JdbcPagingItemReader<>();
		r.setDataSource(ds);
		r.setQueryProvider(qp);
		r.setRowMapper(new CustomerRowMapper());
		r.setPageSize(1000);
		r.setFetchSize(1000);
		r.setName("customerReader");
		r.setSaveState(true);
		return r;
	}
}