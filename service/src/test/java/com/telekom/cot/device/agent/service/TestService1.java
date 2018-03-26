package com.telekom.cot.device.agent.service;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;

public class TestService1 extends AbstractAgentService {

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
}
