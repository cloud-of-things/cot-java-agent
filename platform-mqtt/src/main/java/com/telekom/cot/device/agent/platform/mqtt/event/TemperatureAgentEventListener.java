package com.telekom.cot.device.agent.platform.mqtt.event;

import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;

public class TemperatureAgentEventListener extends PublishedValuesAgentEventListener<String, TemperatureAgentEvent> {

    public String create(PublishedValues publishedValues) {
        return publishedValues.getValue("id");
    }
    
    @Override
    public Class<TemperatureAgentEvent> getEventClass() {
        return TemperatureAgentEvent.class;
    }
}
