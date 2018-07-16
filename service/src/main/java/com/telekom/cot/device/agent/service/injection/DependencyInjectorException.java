package com.telekom.cot.device.agent.service.injection;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;


public class DependencyInjectorException extends AbstractAgentException {

    private static final long serialVersionUID = 3436963084458097662L;

    public DependencyInjectorException(String message) {
        super(message);
    }

    public DependencyInjectorException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
