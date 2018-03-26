package com.telekom.cot.device.agent.operation.softwareupdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;


public class WindowsTestSoftwareInstaller extends SoftwareInstaller {

    private static final Logger LOGGER = LoggerFactory.getLogger(WindowsTestSoftwareInstaller.class);

    protected WindowsTestSoftwareInstaller() throws AbstractAgentException {
        super();
    }

    @Override
    public void installSoftware() throws AbstractAgentException {
        LOGGER.warn("installing software is not implemented, only for tests under windows operating systems");
    }
}
