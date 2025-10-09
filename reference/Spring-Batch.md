# Spring Batch Listener
- 리스너는 배치 처리의 주요 순간들을 관찰하고, 각 시점에 필요한 동작을 정의할 수 있는 도구
- 배치 처리 중 발생하는 특정 이벤트를 감지하고, 원하는 로직을 실행할 수 있음
- Job/Step 시작 전후, 청크 단위, 아이템 단위 처리 시점까지 모든 과정에 개입 가능

---

Spring Batch가 제공하는 리스너를 보자.

## JobExecutionListener
- Job 실행의 시작과 종료 시점에 호출되는 리스너 인터페이스

```java
public interface JobExecutionListener {
    default void beforeJob(JobExecution jobExecution) {}
    default void afterJob(JobExecution jobExecution) {}
}
```

### beforeJob()
- Job 실행 직전에 호출
- Job 시작 전, 필요한 리소르를 준비하는 등의 초기화 작업 수행

### afterJob()
- Job 실행 후에 호출
- Job 실행 결과를 이메일로 전송하거나
- Job이 끝난 후 리소스를 정리하는 등의 부가 작업 수행
- Job의 실행 정보가 메타데이터 저장소에 저장되기 직전에 호출됨
- 이를 활용하면 특정 조건에 따라 실행된 Job의 상태를 변경할 수 있음

---

## StepExecutionListener
- Step 실행의 시작과 종료 시점에 호출되는 리스너 인터페이스
- Step의 시작 시간, 종료 시간, 처리된 데이터 수를 로그로 기록하는 등의 사용자 정의 작업 추가할 수 있음

```java
public interface StepExecutionListener extends StepListener {
    default void beforeStep(StepExecution stepExecution) {}
    
    @Nullable
    default ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }
    
}
```

---

## ChunkListener
- 청크 지향 처리는 청크 단위로 아이템 읽기/쓰기를 반복함
- 이러한 하나의 청크 단위 처리가 시작되기 전, 완료된 후, 그리고 실행 도중 에러가 발생했을 때 호출되는 리스너 인터페이스
- 각 청크의 처리 현황을 모니터링하거나 로깅하는데 사용

```java
public interface ChunkListener extends StepListener {
    default void beforeChunk(ChunkContext context) {}
    default void afterChunk(ChunkContext context) {}
    default void afterCuhnkError(ChunkContext context) {}
}
```

### afterChunk()
- 트랜잭션이 커밋된 후에 호출됨

### afterChunkError()
- 청크 처리 도중 예외가 발생하면 호출됨
- 이는 청크 트랜잭션이 롤백된 이후에 호출됨

---

## ItemReadListener, ItemProcessListener, ItemWriteListener
- 아이템의 읽기, 처리, 쓰기 작업이 수행되는 시점에 호출되는 리스너 인터페이스
- 아이템 단위의 처리 전후와 에러 발생 시점에 호출됨

```java
public interface ItemReadListener<T> extends StepListener {
    default void beforeRead() {}
    default void afterRead(T item) {}
    default void onReadError(Exception ex) {}
}

public interface ItemProcessListener<T, S> extends StepListener {
    default void beforeProcess(T item) {}
    default void afterProcess(T item, @Nullable S result) {}
    default void onProcessError(T item, Exception e) {}
}

public interface ItemWriteListener<S> extends StepListener {
    default void beforeWrite(Chunk<? extends S> items) {}
    default void afterWrite(Chunk<? extends S> items) {}
    default void onWriteError(Exception exception, Chunk<? extends S> items) {}
}
```

### ItemReadListener.afterRead()
- ItemReader.read() 호출 후에 호출되지만
- ItemReader.read() 메서드가 더 이상 읽을 데이터가 없어 `null`을 반환할 때는 호출되지 않음

### ItemProcessListener.afterProcess()
- 반면 ItemProcessor.process() 메서드가 `null`을 반환하더라도 호출됨
- `null`을 반환하는 것은 해당 데이터를 필터링하겠다는 것

### ItemWriteListener.afterWrite()
- 트랜잭션이 커밋되기 전, ChunkListener.afterChunk()가 호출되기 전에 호출됨

---

## 리스너 활용 사례
- 단계별 모니터링과 추적
- 실행 결과에 따른 후속 처리
- 데이터 가공과 전달
- 부가 기능 분리

---

## 배치 리스너 구현 방법
1. 전용 리스너 인터페이스 직접 구현
2. 리스너 특화 어노테이션 사용

## 인터페이스 구현
```java
public class BigBrotherJobExecutionListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("===== 시스템 감시 시작 : 모든 작업을 내 통제 하에 둔다. =====");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("===== 작업 종료, 할당된 자원 정리 완료 =====");
        log.info("===== 시스템 상태: {}", jobExecution.getStatus());
    }

}

public class BigBrotherStepExecutionListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("===== Step 구역 감시 시작 : 모든 행동이 기록된다. =====");
    }

    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("===== STEP 감시 종료, 모든 행동이 기록되었다. =====");
        log.info("===== Big Brother의 감시망에서 벗어날 수 없을 것이다. ======");

        return ExitStatus.COMPLETED;
    }

}
```

- 각 리스너 인터페이스의 메서드들이 모두 `default`로 정의되어 있어서
- 필요한 시점의 메서드만 골라서 구현하면 됨

```java
@Bean
public Job systemMonitoringJob(JobRepository jobRepository, Step monitoringStep) {
    return new JobBuilder("systemMonitoringJob", jobRepository)
        .listener(new BigBrotherJobExecutionListener())
        .start(monitoringStep)
        .build();
}
```

- `JobBuilder`의 `listener()` 메서드를 사용해 리스너를 지정

## 어노테이션 기반 구현
- 간단하고 빠르게 리스너를 구현할 수 있음
- 다음과 같은 어노테이션 제공
```java
@BeforeChunk, @AfterChunk, @AfterChunkError
@BeforeJob, @AfterJob
@BeforeStep, @AfterStep
@AfterRead, @AfterProcess, @AfterWrite, @BeforeRead, @BeforeProcess, @BeforeWrite
@OnReadError, @OnProcessError
@OnSkipInProcess, @OnSkipInRead, @OnSkipInWrite // 스브링 배치의 건너뛰기 이벤트 발생 시 호출
```

```java
@Component
public class ServerRoomInfiltrationListener {
    @BeforeJob
    public void infiltrateServerRoom(JobExecution jobExecution) {
        log.info("===== 판교 서버실 침투 시작 : 보안 시스템 무력화 진행중 =====");
    }
    
    @AfterJob
    public void escapeServerRoom(JobExecution jobExecution) {
        log.info("===== 파괴 완료, 침투 결과: {} =====", jobExecution.getStatus());
    }
}

@Component
public class ServerRackControlListener {
    @BeforeStep
    public void accessServerRack(StepExecution stepExecution) {
        log.info("===== 서버랙 접근 시작, 콘센트 찾는 중 =====");
    }

    @AfterStep
    public ExitStatus leaveServerRack(StepExecution stepExecution) {
        log.info("===== 코드를 뽑아버렸다 =====");
        return new ExitStatus("POWER_DOWN");
    }
}
```

- `@AfterStep`이 선언되어 있는 메서드의 반환타입은 `ExitStatus`임
- `StepExecutionListener`의 `afterStep()` 메서드가 `ExitStatus`를 반환하기 때문에 지켜줘야함

```java
@Bean
public Step serverRackControlStep(Tasklet destructiveTasklet) {
    return new StepBuilder("serverRackControlStep", jobRepository)
        .tasklet(destructiveTasklet(), transactionManager)
        .listener(new ServerRackControlListener())
        .build();
}
```

- 어노테이션을 사용한 리스너도 빌더의 `listener()` 메서드를 통해 설정 가능

---

## JobExecutionListener와 ExecutionContext를 활용한 동적 데이터 전달
- 잡 파라미터만으로 전달할 수 없는 동적인 데이터가 필요한 경우 존재
- `JobExecutionListener`의 `beforeJob()` 메서드를 활용하여 추가적인 동적 데이터를 각 Step의 전달할 수 있음
- Job 수준 ExecutionContext에 저장된 데이터는 해당 Job이 실행하는 모든 Step에서 접근 가능