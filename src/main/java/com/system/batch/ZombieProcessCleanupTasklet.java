package com.system.batch;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZombieProcessCleanupTasklet implements Tasklet {

    private final int processesToKill = 10;
    private int killedProcesses = 0;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        killedProcesses++;
        log.info("===== 프로세스 강제 종료 .. ({}/{}) =====", killedProcesses, processesToKill);

        if (killedProcesses >= processesToKill) {
            log.info("===== 시스템 안정화 완료: 모든 좀비 프로세스 제거 =====");
            return RepeatStatus.FINISHED;
        }

        return RepeatStatus.CONTINUABLE;
    }

}
