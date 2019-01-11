package com.telekom.cot.device.agent.platform.mqtt.event;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;
import com.telekom.cot.device.agent.platform.objects.operation.Operation;
import com.telekom.cot.device.agent.platform.objects.operation.Operation.OperationStatus;

public class OperationTestOperationAgentEventListener
                extends PublishedValuesAgentEventListener<Operation, OperationTestOperationAgentEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationTestOperationAgentEventListener.class);
    private static final String ATTR_C8Y_TEST_OPERATION_GIVEN_STATUS = "c8y_TestOperation.givenStatus";
    private final ConcurrentLinkedQueue<Operation> pendingOperations;
    private static final String STATUS = "status";

    public OperationTestOperationAgentEventListener(final ConcurrentLinkedQueue<Operation> pendingOperations) {
        this.pendingOperations = pendingOperations;
    }

    @Override
    void handle(Operation operation) {
        pendingOperations.add(operation);
    }

    /**
     * Create a ManagedObject by the PublishedValues from the PublishCallback.
     */
    public Operation create(PublishedValues publishedValues) {
        if (!publishedValues.isValid() || !isOperation(publishedValues)) {
            return null;
        }
        
        Operation operation = new Operation(publishedValues.getValue("id")) {};
        operation.setStatus(OperationStatus.valueOf(publishedValues.getValue("status")));
        Map<String, String> testOperationAttrs = new HashMap<>();
        testOperationAttrs.put("givenStatus", publishedValues.getValue(ATTR_C8Y_TEST_OPERATION_GIVEN_STATUS));
        operation.setProperty("c8y_TestOperation", testOperationAttrs);
        return operation;
    }

    private boolean isOperation(PublishedValues values) {
        LOGGER.debug("is templateId an operation: {}", values.getTemplateId());
        return values.contains(STATUS) && !("SUCCESSFUL".equals(values.getValue(STATUS))
                        || "FAILED".equals(values.getValue(STATUS))); //
    }

    @Override
    public Class<OperationTestOperationAgentEvent> getEventClass() {
        return OperationTestOperationAgentEvent.class;
    }
}
