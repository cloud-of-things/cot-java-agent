package com.telekom.cot.device.agent.platform.mqtt.event;

import com.telekom.cot.device.agent.platform.mqtt.PublishCallback;
import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;

public class OperationTestOperationAgentEvent extends PublishedValuesAgentEvent {

    /**
     * Create a instance of a OperationTestOperationAgentEvent.
     * 
     * @param source
     *            the source of fired event
     * @param publishedValues
     *            contains the value from the template response created by the TemplateResponseReader.
     */
    public OperationTestOperationAgentEvent(PublishCallback source, PublishedValues publishedValues) {
        super(source, publishedValues);
    }
}