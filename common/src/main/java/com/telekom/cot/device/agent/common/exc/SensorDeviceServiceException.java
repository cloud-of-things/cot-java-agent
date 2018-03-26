package com.telekom.cot.device.agent.common.exc;

public class SensorDeviceServiceException extends AbstractAgentException {

	private static final long serialVersionUID = 1531810598086650723L;

	public SensorDeviceServiceException(String message, Throwable throwable, boolean enableSuppression,	boolean writableStackTrace) {
		super(message, throwable, enableSuppression, writableStackTrace);
	}

	public SensorDeviceServiceException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
	public SensorDeviceServiceException(String message) {
		super(message);
	}
}
