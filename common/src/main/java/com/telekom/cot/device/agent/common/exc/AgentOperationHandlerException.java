package com.telekom.cot.device.agent.common.exc;

public class AgentOperationHandlerException extends AbstractAgentException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 7752808930953738383L;

    public AgentOperationHandlerException(String message) {
        super(message);
    }

    public AgentOperationHandlerException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
