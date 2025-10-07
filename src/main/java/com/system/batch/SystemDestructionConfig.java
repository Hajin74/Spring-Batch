package com.system.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class SystemDestructionConfig {

    @Bean
    public Job systemDestructionJob(
        JobRepository jobRepository,
        Step systemDestructionStep,
        SystemDestructionValidator validator) {
        return new JobBuilder("systemDestructionJob", jobRepository)
            .validator(validator)
            .start(systemDestructionStep)
            .build();
    }

    @Bean
    public Step systemDestructionStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, SystemDestructionTasklet systemDestructionTasklet) {
        return new StepBuilder("systemDestructionStep", jobRepository)
            .tasklet(systemDestructionTasklet, transactionManager)
            .build();
    }

}
