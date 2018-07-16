package com.telekom.cot.device.agent.common.exc;


public class DeviceCredentialsServiceException extends AbstractAgentException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    public DeviceCredentialsServiceException(String message, Throwable throwable, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, throwable, enableSuppression, writableStackTrace);
    }

    public DeviceCredentialsServiceException(String message, Throwable throwable) {
        super(message, throwable);
    }
    
    public DeviceCredentialsServiceException(String message) {
        super(message);
    }
    
}
