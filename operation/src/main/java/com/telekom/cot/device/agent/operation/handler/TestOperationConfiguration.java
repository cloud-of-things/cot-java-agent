package com.telekom.cot.device.agent.operation.handler;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.telekom.cot.device.agent.common.annotations.ConfigurationPath;
import com.telekom.cot.device.agent.common.configuration.Configuration;

@ConfigurationPath("agent.operations.testOperation")
public class TestOperationConfiguration implements Configuration {

    @NotNull @Positive
    private int delay;

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

}
