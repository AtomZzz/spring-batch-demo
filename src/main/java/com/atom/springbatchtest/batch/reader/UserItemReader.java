package com.atom.springbatchtest.batch.reader;

import com.atom.springbatchtest.batch.model.User;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.StepScope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

@Component
public class UserItemReader {
    @Bean
    @StepScope
    public FlatFileItemReader<User> reader(@Value("#{stepExecutionContext['fileName']}") String fileName) {
        return new FlatFileItemReaderBuilder<User>()
                .name("userItemReader")
                .resource(new FileSystemResource(fileName))
                .linesToSkip(1)
                .delimited()
                .names("id", "name", "email")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(User.class);
                }})
                .build();
    }
} 