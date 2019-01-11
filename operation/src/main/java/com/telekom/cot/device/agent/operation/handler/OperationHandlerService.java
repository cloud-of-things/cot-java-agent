package com.telekom.cot.device.agent.operation.handler;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.platform.objects.operation.Operation;
import com.telekom.cot.device.agent.platform.objects.operation.Operation.OperationStatus;
import com.telekom.cot.device.agent.service.AgentService;


public interface OperationHandlerService<T extends Operation> extends AgentService {

    /**
     * get the type of the operation this handler supports 
     * @return type of supported operation
     */
    public Class<T> getSupportedOperationType();

    /**
     * executes the given operation and returns the execution status
     * @param operation the operation to execute
     * @return the status of operation execution
     * @throws AbstractAgentException
     */
    public OperationStatus execute(T operation) throws AbstractAgentException;
}
