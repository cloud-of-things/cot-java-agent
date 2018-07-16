package com.telekom.cot.device.agent.platform.mqtt.event;

import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;
import com.telekom.cot.device.agent.service.event.AgentEvent;

/**
 * This event is fired when a operation is published from the CoT.
 *
 */
public class OperationAgentEvent extends AgentEvent {

    /** The event source. */
    private Object source;
    /** The operation values. */
    private PublishedValues values;

    public OperationAgentEvent(Object source, PublishedValues values) {
        this.values = values;
    }

    /**
     * Get published values from CoT.
     * 
     * @return the values
     */
    public PublishedValues getPublishedValues() {
        return values;
    }

    /**
     * Get the source.
     */
    @Override
    public Object getSource() {
        return source;
    }
}
