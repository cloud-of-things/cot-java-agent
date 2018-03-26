package com.telekom.cot.device.agent.common.exc;

public class AgentCredentialsNotFoundException extends AbstractAgentException {

	private static final long serialVersionUID = 7456020508140733118L;

	public AgentCredentialsNotFoundException(String message, Throwable throwable, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, throwable, enableSuppression, writableStackTrace);
	}

	public AgentCredentialsNotFoundException(String message, Throwable throwable) {
		super(message, throwable);
	}	
	
	public AgentCredentialsNotFoundException(String message) {
		super(message);
	}
}
