package com.telekom.cot.device.agent.common.exc;

public class AgentCredentialsReadException extends AbstractAgentException {


	/**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1702256925507911828L;

    public AgentCredentialsReadException(String message, Throwable throwable, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, throwable, enableSuppression, writableStackTrace);
	}

	public AgentCredentialsReadException(String message, Throwable throwable) {
		super(message, throwable);
	}	
	
	public AgentCredentialsReadException(String message) {
		super(message);
	}
}
