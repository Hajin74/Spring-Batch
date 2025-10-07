package com.system.batch;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.stereotype.Component;

@Component
public class SystemDestructionValidator implements JobParametersValidator {

    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        if (parameters == null) {
            throw new JobParametersInvalidException("파라미터가 NULL입니다");
        }

        Long destructionLevel = parameters.getLong("destructionLevel");
        if (destructionLevel == null) {
            throw new JobParametersInvalidException("destructionPower 파라미터는 필수값입니다");
        }

        if (destructionLevel > 9) {
            throw new JobParametersInvalidException(
                "파괴력 수준이 허용치를 초과했습니다: " + destructionLevel + " (최대 허용치: 9)");
        }
    }

}
