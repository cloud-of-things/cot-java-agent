package com.telekom.cot.device.agent.platform.mqtt.event;

import com.telekom.cot.device.agent.platform.mqtt.PublishCallback;
import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;

public class GetOperationStatusAgentEvent extends PublishedValuesAgentEvent {

    public GetOperationStatusAgentEvent(PublishCallback source, PublishedValues publishedValues) {
        super(source, publishedValues);
    }
}