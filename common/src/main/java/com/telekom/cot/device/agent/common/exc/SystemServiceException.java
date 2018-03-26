package com.telekom.cot.device.agent.common.exc;

public class SystemServiceException extends AbstractAgentException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	public SystemServiceException(String message, Throwable throwable, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, throwable, enableSuppression, writableStackTrace);
	}

	public SystemServiceException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
	public SystemServiceException(String message) {
		super(message);
	}
	
}
