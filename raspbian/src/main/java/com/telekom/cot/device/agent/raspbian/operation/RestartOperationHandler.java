package com.telekom.cot.device.agent.raspbian.operation;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.*;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.OperationHandlerServiceException;
import com.telekom.cot.device.agent.operation.handler.OperationHandlerService;
import com.telekom.cot.device.agent.operation.operations.RestartOperation;
import com.telekom.cot.device.agent.platform.objects.operation.Operation.OperationStatus;
import com.telekom.cot.device.agent.service.AbstractAgentService;


public class RestartOperationHandler extends AbstractAgentService implements OperationHandlerService<RestartOperation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestartOperationHandler.class);

    private Runtime runtime;
    private boolean isLinuxOs;
    
    @Override
    public void start() throws AbstractAgentException {
        runtime = Runtime.getRuntime();
        isLinuxOs = SystemUtils.IS_OS_LINUX;
    }

    @Override
    public Class<RestartOperation> getSupportedOperationType() {
        return RestartOperation.class;
    }

    @Override
    public OperationStatus execute(RestartOperation operation) throws AbstractAgentException {
        assertNotNull(operation, OperationHandlerServiceException.class, LOGGER, "no operation to execute given");
        LOGGER.debug("start operation executing");
        
        if (!isLinuxOs) {
            throw new OperationHandlerServiceException("restart operations are only supported on linux systems");
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
