package com.telekom.cot.device.agent.common.exc;

public class AgentServiceProxyException extends AbstractAgentException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -2561586217277009652L;

    public AgentServiceProxyException(String message, Throwable throwable) {
        super(message, throwable);
    }
    
    public AgentServiceProxyException(String message) {
        super(message);
    }

}
