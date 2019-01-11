package com.telekom.cot.device.agent.operation.handler;

import javax.validation.constraints.NotNull;

import com.telekom.cot.device.agent.common.ChecksumAlgorithm;
import com.telekom.cot.device.agent.common.annotations.ConfigurationPath;
import com.telekom.cot.device.agent.common.configuration.Configuration;

@ConfigurationPath("agent.operations.softwareUpdate")
public class SoftwareUpdateConfiguration implements Configuration {

    @NotNull
    private ChecksumAlgorithm checksumAlgorithm;

    public ChecksumAlgorithm getChecksumAlgorithm() {
        return checksumAlgorithm;
    }

    public void setChecksumAlgorithm(ChecksumAlgorithm checksumAlgorithm) {
        this.checksumAlgorithm = checksumAlgorithm;
    }
}
