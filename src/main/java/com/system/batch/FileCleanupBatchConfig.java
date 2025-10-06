package com.system.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FileCleanupBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public FileCleanupBatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Tasklet deleteOldFilesTasklet() {
        return new DeletedOldFilesTasklet("temp", 30);
    }

    @Bean
    public Step deleteOldFilesStep() {
        return new StepBuilder("deleteOldFilesStep", jobRepository)
            .tasklet(deleteOldFilesTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Job deleteOldFilesJob() {
        return new JobBuilder("deleteOldFilesJob", jobRepository)
            .start(deleteOldFilesStep())
            .build();
    }

    @Bean
    public Step deleteOldRecordsStep() {
        // 간단한 작업이라 람다식으로 구현
        return new StepBuilder("deleteOldRecordsStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                //int deleted = jdbcTemplete.update("DELETE FROM logs WHERE created < NOW() - INTERNAL 7 DAY");
                //log.info("===== {}개의 오래된 레코드가 삭제되었습니다. =====", deleted);
                log.info("===== 오래된 레코드가 삭제되었습니다. =====");
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

    @Bean
    public Job deleteOldRecordsJob() {
        return new JobBuilder("deleteOldRecordsJob", jobRepository)
            .start(deleteOldRecordsStep()) // Step을 Job에 등록
            .build();
    }
}
