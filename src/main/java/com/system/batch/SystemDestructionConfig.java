package com.system.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
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
        Step systemDestructionStep
    ) {
        return new JobBuilder("systemDestructionJob", jobRepository)
            .validator(new DefaultJobParametersValidator( // 파라미터 존재 여부만 검증할 때 사용
                new String[]{"destructionPower"},  // 필수 파라미터
                new String[]{"targetSystem"}       // 선택적 파라미터
            ))
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
