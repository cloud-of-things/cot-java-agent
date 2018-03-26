package com.telekom.cot.device.agent.operation.handler;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.service.AgentService;

public class TestAgentOperationsHandlerService extends AgentOperationsHandlerService {

    private PlatformService mockPlatformService;

    public TestAgentOperationsHandlerService(PlatformService mockPlatformService) {
        this.mockPlatformService = mockPlatformService;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends AgentService> T getService(Class<T> serviceType) throws AbstractAgentException {
        return (T) mockPlatformService;
    }
}
