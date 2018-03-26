package com.telekom.cot.device.agent.common.exc;

public class AgentShutdownException extends AbstractAgentException {

	private static final long serialVersionUID = -2216889915047934025L;

	public AgentShutdownException(String message, Throwable throwable, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, throwable, enableSuppression, writableStackTrace);
	}

	public AgentShutdownException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public AgentShutdownException(String message) {
		super(message);
	}

}
