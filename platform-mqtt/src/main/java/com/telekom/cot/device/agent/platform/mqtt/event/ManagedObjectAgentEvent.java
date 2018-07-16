package com.telekom.cot.device.agent.platform.mqtt.event;

import com.telekom.cot.device.agent.platform.mqtt.PublishCallback;
import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;

/**
 * Is fired by TemplateId MANAGED_OBJECT_ID_RES.
 */
public class ManagedObjectAgentEvent extends PublishedValuesAgentEvent {

    /**
     * Create a instance of a ManagedObjectAgentEvent.
     * 
     * @param source
     *            the source of fired event
     * @param publishedValues
     *            contains the value from the template response created by the TemplateResponseReader.
     */
    public ManagedObjectAgentEvent(PublishCallback source, PublishedValues publishedValues) {
        super(source, publishedValues);
    }
}
