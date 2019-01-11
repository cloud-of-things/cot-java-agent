package com.telekom.cot.device.agent.common.exc;

public class MeasurementServiceException extends AbstractAgentException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	public MeasurementServiceException(String message, Throwable throwable, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, throwable, enableSuppression, writableStackTrace);
	}

	public MeasurementServiceException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
	public MeasurementServiceException(String message) {
		super(message);
	}
	
}
