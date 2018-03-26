package com.telekom.cot.device.agent.raspbian.operation;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentOperationHandlerException;
import com.telekom.cot.device.agent.operation.handler.OperationExecuteBuilder;
import com.telekom.cot.device.agent.operation.handler.OperationsHandlerService;
import com.telekom.cot.device.agent.service.AbstractAgentService;
import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;

public class RaspbianOperationsHandler extends AbstractAgentService implements OperationsHandlerService {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(RaspbianOperationsHandler.class);

    /**
     * The operation type is the key to executor implementation.
     */
    public enum OperationType {

        C8Y_RESTART("c8y_Restart");

        private String attribute;

        private OperationType(String attribute) {
            this.attribute = attribute;
        }

        public String getAttribute() {
            return attribute;
        }

        public static OperationType findByAttributes(Set<String> attributes) {
            return Arrays.asList(OperationType.values()).stream()
                    .filter(o -> attributes.contains(o.getAttribute()))
                    .findFirst()
                    .get();
        }

        public static List<String> attributes() {
            return Arrays.asList(OperationType.values()).stream()
                    .map(OperationType::getAttribute)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public OperationStatus execute(Operation operation) throws AbstractAgentException {
        LOGGER.info("start operation executing");
        
        // check the operation
        assertNotNull(operation, AgentOperationHandlerException.class, LOGGER, "operation is required");
        assertNotEmpty(operation.getAttributes(), AgentOperationHandlerException.class, LOGGER,
                "operation has no attributes to identify the executor");
        
        // get and check operation type
        try {
            OperationType.findByAttributes(operation.getAttributes().keySet());
        } catch(Exception e) {
            throw createExceptionAndLog(AgentOperationHandlerException.class, LOGGER, "did not found the operation type", e);
        }
        
        // currently only 'C8Y_RESTART' is supported
        return OperationExecuteBuilder.create(operation).setExecutorClass(RestartOperationExecute.class).build().perform();
    }

    @Override
    public String[] getSupportedOperations() {
        return OperationType.attributes().toArray(new String[] {});
    }
}
