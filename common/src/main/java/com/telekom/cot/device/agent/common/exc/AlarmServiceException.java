package com.telekom.cot.device.agent.common.exc;


public class AlarmServiceException extends AbstractAgentException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    public AlarmServiceException(String message, Throwable throwable, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, throwable, enableSuppression, writableStackTrace);
    }

    public AlarmServiceException(String message, Throwable throwable) {
        super(message, throwable);
    }
    
    public AlarmServiceException(String message) {
        super(message);
    }
    
}
