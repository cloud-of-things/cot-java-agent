package com.telekom.cot.device.agent.common.exc;

public class DeviceServiceException extends AbstractAgentException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	public DeviceServiceException(String message, Throwable throwable, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, throwable, enableSuppression, writableStackTrace);
	}

	public DeviceServiceException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
	public DeviceServiceException(String message) {
		super(message);
	}
	
}
