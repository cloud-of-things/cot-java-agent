package com.telekom.cot.device.agent.common.exc;

public class AgentPublishEventException extends AbstractAgentException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -6709941104623780399L;

    public AgentPublishEventException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public AgentPublishEventException(String message) {
        super(message);
    }
}
