package com.telekom.cot.device.agent.operation.handler;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentOperationHandlerException;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;

public class TestOperationExecute extends AbstractOperationExecute<TestOperationConfig> {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TestOperationExecute.class);

    public static final String PARAM_DELAY = "param.delay.in.seconds";

    public enum GivenStatus {
        GIVEN_SUCCESSFUL, GIVEN_FAILED_BY_STATUS, GIVEN_FAILED_BY_EXCEPTION
    }

    @Override
    public OperationStatus perform() throws AbstractAgentException {
        LOGGER.info("perform c8y_TestOperation {}", Objects.nonNull(getOperation()) ? getOperation().getAttributes() : "null");

        // delay execution
        int delayInSeconds = getConfiguration().getDelay();
        try {
            TimeUnit.SECONDS.sleep(delayInSeconds);
        } catch (InterruptedException e) {
            throw new AgentOperationHandlerException("could not delay the execution");
        }
        // perform execution with given status
        switch (OperationUtil.getGivenStatus(getOperation())) {
        case GIVEN_SUCCESSFUL:
            LOGGER.info("executed c8y_TestOperation with status 'SUCCESSFUL'");
            return OperationStatus.SUCCESSFUL;
        case GIVEN_FAILED_BY_STATUS:
            LOGGER.info("executed c8y_TestOperation with status 'FAILED'");
            return OperationStatus.FAILED;
        case GIVEN_FAILED_BY_EXCEPTION:
            LOGGER.info("executed c8y_TestOperation with status 'FAILED_BY_EXCEPTION'");
            throw new AgentOperationHandlerException("execution is failed by exception");
        default:
            LOGGER.info("can't execute c8y_TestOperation, unknown given status");
            return OperationStatus.FAILED;
        }
    }

}
