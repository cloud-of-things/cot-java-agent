package com.telekom.cot.device.agent.operation.handler;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.platform.objects.Operation;
import com.telekom.cot.device.agent.platform.objects.OperationStatus;
import com.telekom.cot.device.agent.service.AgentService;

public interface OperationsHandlerService extends AgentService {

	/**
	 * gets the names of operations supported by this handler 
	 * @return names of supported operations
	 */
	public String[] getSupportedOperations();

	/**
	 * executes the given operation and returns the execution status
	 * @param operation the operation to execute
	 * @return the status of operation execution
	 * @throws AbstractAgentException
	 */
	public OperationStatus execute(Operation operation) throws AbstractAgentException;
}
