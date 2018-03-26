package com.telekom.cot.device.agent.operation.handler;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;

public interface OperationExecuteCallback<C> {

    public void finished(C content) throws AbstractAgentException;
    
}
