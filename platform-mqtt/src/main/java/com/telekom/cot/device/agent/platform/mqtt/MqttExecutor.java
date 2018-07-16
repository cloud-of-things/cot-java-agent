package com.telekom.cot.device.agent.platform.mqtt;

import com.telekom.cot.device.agent.platform.mqtt.event.PublishedValuesAgentEventListener;
import com.telekom.cot.device.agent.service.event.AgentContext;

public class MqttExecutor<T extends PublishedValuesAgentEventListener> {

    public MqttExecutor(MqttPlatform mqttPlatform, PublishCallback publishCallback, AgentContext agentContext,
                    T listener) {
    }

    public void execut(String templateResponse) {
    }
}
