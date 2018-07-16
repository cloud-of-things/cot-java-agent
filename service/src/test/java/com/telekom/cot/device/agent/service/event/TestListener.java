package com.telekom.cot.device.agent.service.event;

import com.telekom.cot.device.agent.service.event.AgentEventListener;

public class TestListener extends AbstractService implements TestPlatformService, AgentEventListener<TestEvent> {

    private boolean executed = false;

    @Override
    public void onAgentEvent(TestEvent event) {
        executed = true;
    }

    public boolean isExecuted() {
        return executed;
    }
}
