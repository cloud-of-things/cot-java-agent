package com.telekom.cot.device.agent.common.exc;

public class OperationServiceException extends AbstractAgentException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 4654309513173239856L;

	public OperationServiceException(String message, Throwable throwable, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, throwable, enableSuppression, writableStackTrace);
	}

	public OperationServiceException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public OperationServiceException(String message) {
		super(message);
	}

}
