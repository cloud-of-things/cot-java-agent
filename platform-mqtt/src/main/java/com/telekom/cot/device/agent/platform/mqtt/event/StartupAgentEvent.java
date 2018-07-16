package com.telekom.cot.device.agent.platform.mqtt.event;

import com.telekom.cot.device.agent.platform.mqtt.PublishCallback;
import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;

public class StartupAgentEvent extends PublishedValuesAgentEvent {

    public StartupAgentEvent(PublishCallback source, PublishedValues publishedValues) {
        super(source, publishedValues);
    }
}
