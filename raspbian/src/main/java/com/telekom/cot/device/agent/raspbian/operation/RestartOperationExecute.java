package com.telekom.cot.device.agent.raspbian.operation;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.configuration.Configuration;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentOperationHandlerException;
import com.telekom.cot.device.agent.operation.handler.AbstractOperationExecute;
import com.telekom.cot.device.agent.platform.objects.OperationStatus;

public class RestartOperationExecute extends AbstractOperationExecute<Configuration> {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(RestartOperationExecute.class);

    private final Runtime runtime;
    private final boolean isLinuxOs;

    public RestartOperationExecute() {
        runtime = Runtime.getRuntime();
        isLinuxOs = SystemUtils.IS_OS_LINUX;
    }

    public OperationStatus perform() throws AbstractAgentException {
        LOGGER.info("execute restart operation");
        if (!isLinuxOs) {
            throw new AgentOperationHandlerException("only a restart on a linux system is allowed");
        }
        
        try {
            runtime.exec("sudo reboot &");
            return OperationStatus.EXECUTING;
        } catch (Exception e) {
            LOGGER.error("can't execute restart operation, ", e);
            return OperationStatus.FAILED;
        }
    }
}
