package com.telekom.cot.device.agent.platform.mqtt.event;

import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;

public class StartupAgentEventListener extends PublishedValuesAgentEventListener<String, StartupAgentEvent> {

    public String create(PublishedValues publishedValues) {
        return publishedValues.getValue("id");
    }
    
    @Override
    public Class<StartupAgentEvent> getEventClass() {
        return StartupAgentEvent.class;
    }
}
