package com.telekom.cot.device.agent.common.exc;

public class AgentCredentialsWriteException extends AbstractAgentException {

	private static final long serialVersionUID = -1178260323342237601L;

	public AgentCredentialsWriteException(String message, Throwable throwable, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, throwable, enableSuppression, writableStackTrace);
	}

	public AgentCredentialsWriteException(String message, Throwable throwable) {
		super(message, throwable);
	}	
	
	public AgentCredentialsWriteException(String message) {
		super(message);
	}
}
