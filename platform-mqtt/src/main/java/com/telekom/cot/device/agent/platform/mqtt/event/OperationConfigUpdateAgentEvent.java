package com.telekom.cot.device.agent.platform.mqtt.event;

import com.telekom.cot.device.agent.platform.mqtt.PublishCallback;
import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;

public class OperationConfigUpdateAgentEvent extends PublishedValuesAgentEvent {

    /**
     * Create a instance of a OperationConfigUpdateAgentEvent.
     * 
     * @param source
     *            the source of fired event
     * @param publishedValues
     *            contains the value from the template response created by the TemplateResponseReader.
     */
    public OperationConfigUpdateAgentEvent(PublishCallback source, PublishedValues publishedValues) {
        super(source, publishedValues);
    }
}
