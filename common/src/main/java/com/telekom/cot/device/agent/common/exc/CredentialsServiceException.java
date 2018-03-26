package com.telekom.cot.device.agent.common.exc;

public class CredentialsServiceException extends AbstractAgentException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -5337094373670657544L;

	public CredentialsServiceException(String message, Throwable throwable, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, throwable, enableSuppression, writableStackTrace);
	}

	public CredentialsServiceException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public CredentialsServiceException(String message) {
		super(message);
	}

}
