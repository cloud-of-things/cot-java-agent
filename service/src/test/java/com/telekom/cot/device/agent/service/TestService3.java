package com.telekom.cot.device.agent.service;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.injection.Inject;

public class TestService3 extends AbstractAgentService implements TestServiceIF3 {

    @Inject
    private TestConfiguration configuration;
    
	private boolean started = false;
	
	@Override
	public void start() throws AbstractAgentException {
		started = true;
	}

	@Override
	public void stop() throws AbstractAgentException {
		started = false;
	}

	@Override
	public boolean isStarted() {
		return started;
	}
	
	
    public TestConfiguration getConfiguration() {
        return configuration;
    }
    
}
