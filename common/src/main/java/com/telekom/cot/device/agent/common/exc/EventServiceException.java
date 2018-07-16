package com.telekom.cot.device.agent.common.exc;


public class EventServiceException extends AbstractAgentException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    public EventServiceException(String message, Throwable throwable, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, throwable, enableSuppression, writableStackTrace);
    }

    public EventServiceException(String message, Throwable throwable) {
        super(message, throwable);
    }
    
    public EventServiceException(String message) {
        super(message);
    }
    
}
