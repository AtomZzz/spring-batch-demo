package com.atom.springbatchtest.batch.writer;

import com.atom.springbatchtest.batch.model.User;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class UserItemWriter {
    @Autowired
    private DataSource dataSource;

    @Bean
    public JdbcBatchItemWriter<User> writer() {
        return new JdbcBatchItemWriterBuilder<User>()
                .itemSqlParameterSourceProvider(new org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO user (id, name, email) VALUES (:id, :name, :email)")
                .dataSource(dataSource)
                .build();
    }
} 
 