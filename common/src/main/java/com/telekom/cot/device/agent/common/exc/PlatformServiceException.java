package com.telekom.cot.device.agent.common.exc;

/**
 * The platform service exception.
 *
 */
public class PlatformServiceException extends AbstractAgentException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 6002491290269213339L;

	/** The HTTP status. */
	private final Integer httpStatus;

	/**
	 * If the request was not successful.
	 * 
	 * @param httpStatus
	 * @param message
	 * @param throwable
	 */
	public PlatformServiceException(Integer httpStatus, String message, Throwable throwable) {
		super(message, throwable);
		this.httpStatus = httpStatus;
	}

	/**
	 * If the client side execution was not successful.
	 * 
	 * @param message
	 * @param throwable
	 */
	public PlatformServiceException(String message, Throwable throwable) {
		super(message, throwable);
		httpStatus = 0;
	}

	/**
	 * If the client side execution was not successful.
	 * 
	 * @param message
	 * @param throwable
	 */
	public PlatformServiceException(String message) {
		super(message);
        httpStatus = 0;
	}

	/**
	 * Get the HTTP status.
	 * 
	 * @return the status
	 */
	public Integer getHttpStatus() {
		return httpStatus;
	}

}
