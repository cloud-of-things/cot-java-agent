package com.telekom.cot.device.agent.common.exc;


public class SoftwareInstallerException extends AbstractAgentException {

    private static final long serialVersionUID = 6702182477564985666L;

    public SoftwareInstallerException(String message, Throwable throwable, boolean enableSuppression, boolean writableStackTrace) {
        super(message, throwable, enableSuppression, writableStackTrace);
    }
    
    public SoftwareInstallerException(String message, Throwable throwable) {
        super(message, throwable);
    }
    
    public SoftwareInstallerException(String message) {
        super(message);
    }
}
