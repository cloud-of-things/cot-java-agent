package com.telekom.cot.device.agent.common.exc;

public class AgentClassCastException extends AbstractAgentException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -9076248432684583752L;

    public AgentClassCastException(String message, ClassCastException throwable) {
        super(message, throwable);
    }
}
