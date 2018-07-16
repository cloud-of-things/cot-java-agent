package com.telekom.cot.device.agent.platform.mqtt.event;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;
import com.telekom.cot.device.agent.platform.objects.Operation;

public class OperationConfigUpdateAgentEventListener
                extends PublishedValuesAgentEventListener<Operation, OperationConfigUpdateAgentEvent> {

    private static final String ATTR_C8Y_CONFIGURATION = "c8y_Configuration.config";
    private final ConcurrentLinkedQueue<Operation> pendingOperations;
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationConfigUpdateAgentEventListener.class);

    public OperationConfigUpdateAgentEventListener(final ConcurrentLinkedQueue<Operation> pendingOperations) {
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
            Map<String, String> configurationOperationAttrs = new HashMap<>();
            configurationOperationAttrs.put("config", clear(publishedValues.getValue(ATTR_C8Y_CONFIGURATION)));
            operation.setProperty("c8y_Configuration", configurationOperationAttrs);
            return operation;
        }
        return null;
    }

    private String clear(String value) {
        Pattern pattern = Pattern.compile("\\u0022\\u0022");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            value = matcher.replaceAll("\"");
        }
        return value;
    }

    private boolean isOperation(PublishedValues values) {
        LOGGER.debug("is templateId an operation: {}", values.getTemplateId());
        return values.contains("status") && !(values.getValue("status").equals("SUCCESSFUL")
                        || values.getValue("status").equals("FAILED"));
    }
    
    @Override
    public Class<OperationConfigUpdateAgentEvent> getEventClass() {
        return OperationConfigUpdateAgentEvent.class;
    }
}
