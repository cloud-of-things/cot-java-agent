package com.telekom.cot.device.agent.common.exc;

/**
 * This exception is used in case of a yaml config property not exist. 
 */
public class PropertyNotFoundException extends AbstractAgentException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -8862247048199259293L;

	/**
	 * Create exception with message.
	 * 
	 * @param message
	 */
	public PropertyNotFoundException(String message) {
		super(message);
	}

}
