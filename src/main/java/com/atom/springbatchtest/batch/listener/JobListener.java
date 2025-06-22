package com.atom.springbatchtest.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // 记录开始时间到 Job 的 ExecutionContext 中
        jobExecution.getExecutionContext().putLong("job.startTime", System.currentTimeMillis());
        log.info("作业开始执行: {}", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long endTime = System.currentTimeMillis();
        long startTime = jobExecution.getExecutionContext().getLong("job.startTime", -1);

        String jobName = jobExecution.getJobInstance().getJobName();
        String exitStatus = jobExecution.getExitStatus().getExitCode();

        if (startTime != -1) {
            long duration = endTime - startTime;
            log.info("作业完成: [{}], 状态: [{}], 总耗时: [{} ms]", jobName, exitStatus, duration);
        } else {
            log.warn("作业完成: [{}], 状态: [{}], 但未找到开始时间", jobName, exitStatus);
        }

        // 作业完成后安全关闭应用
        System.exit(0);
    }
}

