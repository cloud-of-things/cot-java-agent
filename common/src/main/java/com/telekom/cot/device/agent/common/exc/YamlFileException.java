package com.telekom.cot.device.agent.common.exc;

public class YamlFileException extends AbstractAgentException {

	/**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1266152955939570940L;

    public YamlFileException(String message, Throwable throwable, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, throwable, enableSuppression, writableStackTrace);
	}

	public YamlFileException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public YamlFileException(String message) {
		super(message);
	}

}
