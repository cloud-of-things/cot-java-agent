package com.telekom.cot.device.agent.platform.mqtt.event;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;
import com.telekom.cot.device.agent.platform.objects.Operation;
import com.telekom.cot.device.agent.platform.objects.OperationStatus;

public class OperationTestOperationAgentEventListener
                extends PublishedValuesAgentEventListener<Operation, OperationTestOperationAgentEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationTestOperationAgentEventListener.class);
    private static final String ATTR_C8Y_TEST_OPERATION_GIVEN_STATUS = "c8y_TestOperation.givenStatus";
    private final ConcurrentLinkedQueue<Operation> pendingOperations;

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
        if (publishedValues.isValid() && isOperation(publishedValues)) {
            Operation operation = new Operation();
            operation.setId(publishedValues.getValue("id"));
            operation.setStatus(OperationStatus.findByName(publishedValues.getValue("status")));
            Map<String, String> testOperationAttrs = new HashMap<>();
            testOperationAttrs.put("givenStatus", publishedValues.getValue(ATTR_C8Y_TEST_OPERATION_GIVEN_STATUS));
            operation.setProperty("c8y_TestOperation", testOperationAttrs);
            return operation;
        }
        return null;
    }

    private boolean isOperation(PublishedValues values) {
        LOGGER.debug("is templateId an operation: {}", values.getTemplateId());
        return values.contains("status") && !(values.getValue("status").equals("SUCCESSFUL")
                        || values.getValue("status").equals("FAILED"));
    }

    @Override
    public Class<OperationTestOperationAgentEvent> getEventClass() {
        return OperationTestOperationAgentEvent.class;
    }
}
