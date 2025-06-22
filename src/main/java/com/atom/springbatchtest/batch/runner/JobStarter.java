package com.atom.springbatchtest.batch.runner;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class JobStarter implements CommandLineRunner {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job fileProcessingJob;

    @Override
    public void run(String... args) throws Exception {
        long time = System.currentTimeMillis();
        System.out.println("Starting batch job : " + time);
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("jobId", time)
                .toJobParameters();
        jobLauncher.run(fileProcessingJob, jobParameters);
        //todo 由于任务是异步执行的，如果数据量过大，这里可能会在任务还没有执行完就退出程序，导致报错。
        // 因此最好的办法是在jobListener中检查Job是否完整后安全关闭应用。
        // System.exit(0);
    }
}