package com.telekom.cot.device.agent.common.exc;

public class ConfigurationNotFoundException extends AbstractAgentException {

	private static final long serialVersionUID = 8871835642355991876L;

	public ConfigurationNotFoundException(String message, Throwable throwable, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, throwable, enableSuppression, writableStackTrace);
	}

	public ConfigurationNotFoundException(String message, Throwable throwable) {
		super(message, throwable);
	}	
	
	public ConfigurationNotFoundException(String message) {
		super(message);
	}
}
