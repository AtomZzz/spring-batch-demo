package com.atom.springbatchtest.batch.config;

import com.atom.springbatchtest.batch.model.User;
import com.atom.springbatchtest.batch.partition.FilePartitioner;
import com.atom.springbatchtest.batch.writer.JdbcBatchItemWriterProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcBatchItemWriterProxy jdbcBatchItemWriterProxy;

    // 1. 线程池配置
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("batch-processor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 设置拒绝策略
        executor.initialize();
        return executor;
    }


    // 3. 分区处理步骤
    @Bean(name = "slaveStep")
    public Step slaveStep() {
        TaskletStep slaveStep = new StepBuilder("slaveStep", jobRepository)
                .<User, User>chunk(500, transactionManager)
                .reader(fileItemReader(null))
                .processor(userProcessor(null))
                .writer(jdbcBatchItemWriterProxy)
                .faultTolerant()
                .skipLimit(500)
                .skip(Exception.class)
                .build();
        log.info("===== 创建从步骤 =====,slaveStep={}", slaveStep.hashCode());
        return slaveStep;
    }

    // 4. 主步骤配置（分区处理）
    @Bean(name = "masterStep")
    public Step masterStep(PartitionHandler partitionHandler, FilePartitioner filePartitioner) {
        log.info("===== 创建主步骤 =====,partitionHandler={}", partitionHandler.hashCode());
        log.info("===== 创建主步骤 =====,filePartitioner={}", filePartitioner.hashCode());
        return new StepBuilder("masterStep", jobRepository)
                .partitioner("slaveStep", filePartitioner)
                .partitionHandler(partitionHandler)
                .build();
    }

    @Bean
    public PartitionHandler partitionHandler(@Autowired @Qualifier("slaveStep") Step slaveStep,
                                             TaskExecutor taskExecutor) {
        var partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setStep(slaveStep);
        partitionHandler.setTaskExecutor(taskExecutor);
        partitionHandler.setGridSize(10);
        log.info("===== 创建分区处理句柄 =====partitionHandler={}", partitionHandler.hashCode());
        return partitionHandler;
    }

    // 5. 作业配置
    @Bean
    public Job fileProcessingJob(@Autowired @Qualifier("masterStep") Step maserStep) {
        return new JobBuilder("multiFileJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(maserStep)
                .build();
    }

    // 6. 文件读取器
    @Bean
    @StepScope
    public FlatFileItemReader<User> fileItemReader(@Value("#{stepExecutionContext['filePath']}") String filePath) {
        System.out.println("===== 正在创建文件读取器 =====");
        System.out.println("线程: " + Thread.currentThread().getName());
        System.out.println("文件路径: " + filePath);
        return new FlatFileItemReaderBuilder<User>()
                .name("userItemReader")
                .resource(new FileSystemResource(filePath))  // 修改为 ClassPathResource
                .delimited()
                .names("id","name", "email")
                .linesToSkip(1) // 跳过CSV表头
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(User.class);
                }})
                .build();
    }

    // 7. 处理器（添加线程信息和文件名）
    @Bean
    @StepScope
    public ItemProcessor<User, User> userProcessor(
            @Value("#{stepExecutionContext['fileName']}") String fileName) {
        return user -> {
            // 添加处理线程信息和来源文件名
            user.setProcessorThread(Thread.currentThread().getName());
            user.setSourceFile(fileName);
            // 处理逻辑
            return user;
        };
    }


}