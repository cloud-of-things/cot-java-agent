package com.telekom.cot.device.agent.common.exc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the root exception. Each concrete exception is extended by this class.
 *
 */
public abstract class AbstractAgentException extends Exception {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAgentException.class);

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -8072126803757800382L;

	/**
	 * Create a abstract exception.
	 * 
	 * @param message
	 * @param throwable
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public AbstractAgentException(String message, Throwable throwable, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, throwable, enableSuppression, writableStackTrace);
		LOGGER.error(this.getClass().getSimpleName() + ": " + message, throwable);
	}

	/**
	 * Create a exception.
	 * 
	 * @param message
	 * @param throwable
	 */
	public AbstractAgentException(String message, Throwable throwable) {
		super(message, throwable);
		LOGGER.error(this.getClass().getSimpleName() + ": " + message, throwable);
	}

	/**
	 * Create a exception.
	 * 
	 * @param message
	 */
	public AbstractAgentException(String message) {
		super(message);
		LOGGER.error(this.getClass().getSimpleName() + ": " + message);
	}

}
