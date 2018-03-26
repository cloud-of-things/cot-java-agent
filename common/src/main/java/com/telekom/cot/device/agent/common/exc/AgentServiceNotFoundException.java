package com.telekom.cot.device.agent.common.exc;

public class AgentServiceNotFoundException extends AbstractAgentException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 7657788985566305016L;
	
	/**
	 * Create exception with message.
	 * 
	 * @param message
	 */
	public AgentServiceNotFoundException(String message) {
		super(message);
	}

}
