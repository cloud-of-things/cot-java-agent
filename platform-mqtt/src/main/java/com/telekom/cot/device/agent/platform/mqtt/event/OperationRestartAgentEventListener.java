package com.telekom.cot.device.agent.platform.mqtt.event;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;
import com.telekom.cot.device.agent.platform.objects.Operation;

public class OperationRestartAgentEventListener
                extends PublishedValuesAgentEventListener<Operation, OperationRestartAgentEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationRestartAgentEventListener.class);
    private final ConcurrentLinkedQueue<Operation> pendingOperations;

    public OperationRestartAgentEventListener(final ConcurrentLinkedQueue<Operation> pendingOperations) {
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
        if (isOperation(publishedValues)) {
            Operation operation = new Operation();
            operation.setId(publishedValues.getValue("id"));
            operation.setProperty("c8y_Restart", "");
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
    public Class<OperationRestartAgentEvent> getEventClass() {
        return OperationRestartAgentEvent.class;
    }
}
