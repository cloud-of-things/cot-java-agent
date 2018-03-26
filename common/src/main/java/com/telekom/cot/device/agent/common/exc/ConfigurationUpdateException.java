package com.telekom.cot.device.agent.common.exc;

public class ConfigurationUpdateException extends AbstractAgentException {

	private static final long serialVersionUID = 1869165592574816897L;

	public ConfigurationUpdateException(String message, Throwable throwable, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, throwable, enableSuppression, writableStackTrace);
	}

	public ConfigurationUpdateException(String message, Throwable throwable) {
		super(message, throwable);
	}	
	
	public ConfigurationUpdateException(String message) {
		super(message);
	}
}
