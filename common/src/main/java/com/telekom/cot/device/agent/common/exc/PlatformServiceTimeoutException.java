package com.telekom.cot.device.agent.common.exc;

/**
 * The platform service exception.
 *
 */
public class PlatformServiceTimeoutException extends AbstractAgentException {

	/**
     * serialVersionUID
     */
    private static final long serialVersionUID = 2356125862428912547L;

	/**
	 * If the client side execution was not successful.
	 * 
	 * @param message
	 * @param throwable
	 */
	public PlatformServiceTimeoutException(String message) {
		super(message);
	}

}
