package com.telekom.cot.device.agent.common.exc;

public class OperationHandlerServiceException extends AbstractAgentException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 7752808930953738383L;

    public OperationHandlerServiceException(String message) {
        super(message);
    }

    public OperationHandlerServiceException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
