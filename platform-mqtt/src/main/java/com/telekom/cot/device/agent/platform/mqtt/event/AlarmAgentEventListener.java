package com.telekom.cot.device.agent.platform.mqtt.event;

import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;

public class AlarmAgentEventListener extends PublishedValuesAgentEventListener<String, AlarmAgentEvent> {

    public String create(PublishedValues publishedValues) {
        return publishedValues.getValue("id");
    }
    
    @Override
    public Class<AlarmAgentEvent> getEventClass() {
        return AlarmAgentEvent.class;
    }
}
