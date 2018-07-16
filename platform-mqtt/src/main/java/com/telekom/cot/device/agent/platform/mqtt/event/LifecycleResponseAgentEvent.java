package com.telekom.cot.device.agent.platform.mqtt.event;

import com.telekom.cot.device.agent.platform.mqtt.PublishCallback;
import com.telekom.cot.device.agent.service.event.AgentEvent;

public class LifecycleResponseAgentEvent extends AgentEvent {

    private PublishCallback publishCallback;
    private Lifecycle lifecycle;
    
    public enum Lifecycle {
        STARTUP, FINISHED
    }

    public LifecycleResponseAgentEvent(PublishCallback publishCallback, Lifecycle lifecycle) {
        this.publishCallback = publishCallback;
        this.lifecycle = lifecycle;
    }
    
    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    @Override
    public Object getSource() {
        return publishCallback;
    }
}
