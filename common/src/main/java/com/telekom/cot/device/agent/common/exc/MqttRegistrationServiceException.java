package com.telekom.cot.device.agent.common.exc;

public class MqttRegistrationServiceException extends AbstractAgentException {

	/**
     * serialVersionUID
     */
    private static final long serialVersionUID = -4697288868951332804L;

    public MqttRegistrationServiceException(String message, Throwable throwable, boolean enableSuppression,
                                            boolean writableStackTrace) {
		super(message, throwable, enableSuppression, writableStackTrace);
	}

	public MqttRegistrationServiceException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public MqttRegistrationServiceException(String message) {
		super(message);
	}

}
