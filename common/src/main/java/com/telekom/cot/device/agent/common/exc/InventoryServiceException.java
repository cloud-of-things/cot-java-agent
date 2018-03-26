package com.telekom.cot.device.agent.common.exc;

public class InventoryServiceException extends AbstractAgentException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 2989358926279226793L;

	public InventoryServiceException(String message, Throwable throwable, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, throwable, enableSuppression, writableStackTrace);
	}

	public InventoryServiceException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public InventoryServiceException(String message) {
		super(message);
	}

}
