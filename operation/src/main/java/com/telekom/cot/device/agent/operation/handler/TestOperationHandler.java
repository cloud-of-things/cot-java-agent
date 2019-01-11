package com.telekom.cot.device.agent.operation.handler;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.OperationHandlerServiceException;
import com.telekom.cot.device.agent.common.injection.Inject;
import com.telekom.cot.device.agent.operation.operations.TestOperation;
import com.telekom.cot.device.agent.operation.operations.TestOperation.GivenStatus;
import com.telekom.cot.device.agent.platform.objects.operation.Operation.OperationStatus;
import com.telekom.cot.device.agent.service.AbstractAgentService;

public class TestOperationHandler extends AbstractAgentService implements OperationHandlerService<TestOperation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestOperationHandler.class);
    
    @Inject
    TestOperationConfiguration configuration;
    
    @Override
    public Class<TestOperation> getSupportedOperationType() {
        return TestOperation.class;
    }

    @Override
    public OperationStatus execute(TestOperation operation) throws AbstractAgentException {
        assertNotNull(operation, OperationHandlerServiceException.class, LOGGER, "no TestOperation given to execute");
        LOGGER.debug("execute TestOperation {}", operation.getProperties());

        // delay execution
        try {
            TimeUnit.SECONDS.sleep(configuration.getDelay());
        } catch (Exception e) {
            throw createExceptionAndLog(OperationHandlerServiceException.class, LOGGER, "can't delay the execution", e);
        }

        // get and check given status
        GivenStatus givenStatus = operation.getGivenStatus();
        if(Objects.isNull(givenStatus)) {
            throw createExceptionAndLog(OperationHandlerServiceException.class, LOGGER, "can't get given status from operation");
        }

        // perform execution with given status
        switch (givenStatus) {
            case GIVEN_SUCCESSFUL:
                LOGGER.info("executed TestOperation with given status 'SUCCESSFUL'");
                return OperationStatus.SUCCESSFUL;
                
            case GIVEN_FAILED_BY_STATUS:
                LOGGER.info("executed TestOperation with given status 'FAILED'");
                return OperationStatus.FAILED;

            case GIVEN_FAILED_BY_EXCEPTION:
                LOGGER.info("executed TestOperation with given status 'FAILED_BY_EXCEPTION'");
                throw new OperationHandlerServiceException("execution is failed by exception");

            default:
                LOGGER.info("can't execute TestOperation, unknown given status");
                return OperationStatus.FAILED;
        }
    }
}
