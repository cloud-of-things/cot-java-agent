package com.telekom.cot.device.agent.operation.softwareupdate;

import javax.validation.constraints.NotNull;

import com.telekom.cot.device.agent.common.ChecksumAlgorithm;
import com.telekom.cot.device.agent.service.configuration.Configuration;

public class SoftwareUpdateConfig implements Configuration {

    @NotNull
    private ChecksumAlgorithm checksumAlgorithm;

    public ChecksumAlgorithm getChecksumAlgorithm() {
        return checksumAlgorithm;
    }

    public void setChecksumAlgorithm(ChecksumAlgorithm checksumAlgorithm) {
        this.checksumAlgorithm = checksumAlgorithm;
    }
}
