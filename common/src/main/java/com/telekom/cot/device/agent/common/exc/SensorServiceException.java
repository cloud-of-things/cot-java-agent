package com.telekom.cot.device.agent.common.exc;

public class SensorServiceException extends AbstractAgentException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	public SensorServiceException(String message, Throwable throwable, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, throwable, enableSuppression, writableStackTrace);
	}

	public SensorServiceException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
	public SensorServiceException(String message) {
		super(message);
	}
	
}
