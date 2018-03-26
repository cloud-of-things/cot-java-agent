package com.telekom.cot.device.agent.operation.handler;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.telekom.cot.device.agent.operation.softwareupdate.SoftwareUpdateConfig;
import com.telekom.cot.device.agent.service.configuration.Configuration;
import com.telekom.cot.device.agent.service.configuration.ConfigurationPath;

@ConfigurationPath("agent.operations")
public class AgentOperationsHandlerConfiguration implements Configuration {

    @Valid
    @NotNull
    private TestOperationConfig testOperation;
    @Valid
    @NotNull
    private SoftwareUpdateConfig softwareUpdate;

    public TestOperationConfig getTestOperation() {
        return testOperation;
    }

    public void setTestOperation(TestOperationConfig testOperation) {
        this.testOperation = testOperation;
    }

    public SoftwareUpdateConfig getSoftwareUpdate() {
        return softwareUpdate;
    }

    public void setSoftwareUpdate(SoftwareUpdateConfig softwareUpdate) {
        this.softwareUpdate = softwareUpdate;
    }
}
