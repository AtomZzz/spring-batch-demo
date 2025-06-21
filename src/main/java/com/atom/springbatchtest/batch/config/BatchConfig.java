package com.atom.springbatchtest.batch.config;


import com.atom.springbatchtest.batch.model.User;
import com.atom.springbatchtest.batch.partition.FilePartitioner;
import com.atom.springbatchtest.batch.processor.UserItemProcessor;
import com.atom.springbatchtest.batch.reader.UserItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserItemReader userItemReader;
    private final ItemWriter<User> writer;

    public BatchConfig(JobRepository jobRepository,
                       PlatformTransactionManager transactionManager,
                       UserItemReader userItemReader,
                       ItemWriter<User> writer) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.userItemReader = userItemReader;
        this.writer = writer;
    }

    @Bean
    public ItemProcessor<User, User> processor() {
        return new UserItemProcessor();
    }

    @Bean
    public FilePartitioner filePartitioner() {
        // 指定你的数据文件目录
        return new FilePartitioner("src/main/resources/data");
    }

    @Bean
    public Step slaveStep(FlatFileItemReader<User> reader, ItemProcessor<User, User> processor) {
        return new StepBuilder("slaveStep", jobRepository)
                .<User, User>chunk(2000, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Step partitionedStep(FilePartitioner filePartitioner, Step slaveStep) {
        return new StepBuilder("partitionedStep", jobRepository)
                .partitioner(slaveStep)
                .partitioner("slaveStep", filePartitioner)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .gridSize(4)
                .build();
    }

    @Bean
    public Job importUserJob(Step partitionedStep) {
        return new JobBuilder("importUserJob", jobRepository)
                .start(partitionedStep)
                .build();
    }
} 