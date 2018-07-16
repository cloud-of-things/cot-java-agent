package com.telekom.cot.device.agent.operation;

import java.util.Objects;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.operation.handler.OperationsHandlerService;
import com.telekom.cot.device.agent.platform.objects.Operation;
import com.telekom.cot.device.agent.platform.objects.OperationStatus;
import com.telekom.cot.device.agent.service.AbstractAgentService;

public class TestOperationHandlerService extends AbstractAgentService implements OperationsHandlerService {

	public enum ExceptionLocation {
		START, EXECUTE, STOP
	}

	private OperationStatus operationStatus;
	private AbstractAgentException agentException;
	private RuntimeException runtimeException;
	private ExceptionLocation location;

	private boolean started = false;
	private boolean stopped = false;
	private boolean executed = false;
	private Operation operation;

	public TestOperationHandlerService(OperationStatus operationStatus) {
		this.operationStatus = operationStatus;
	}

	public TestOperationHandlerService(AbstractAgentException agentException, ExceptionLocation location) {
		this.agentException = agentException;
		this.location = location;
	}

	public TestOperationHandlerService(RuntimeException runtimeException, ExceptionLocation location) {
		this.runtimeException = runtimeException;
		this.location = location;
	}

	public TestOperationHandlerService(AbstractAgentException agentException, ExceptionLocation location,
			OperationStatus operationStatus) {
		this.agentException = agentException;
		this.location = location;
		this.operationStatus = operationStatus;
	}

	public TestOperationHandlerService(RuntimeException runtimeException, ExceptionLocation location,
			OperationStatus operationStatus) {
		this.runtimeException = runtimeException;
		this.location = location;
		this.operationStatus = operationStatus;
	}

	@Override
	public void start() throws AbstractAgentException {
		started = true;
		if (ExceptionLocation.START == location) {
			if (Objects.nonNull(runtimeException)) {
				throw runtimeException;
			} else if (Objects.nonNull(agentException)) {
				throw agentException;
			}
		}
	}

	@Override
	public void stop() throws AbstractAgentException {
		stopped = true;
		if (ExceptionLocation.STOP == location) {
			if (Objects.nonNull(runtimeException)) {
				throw runtimeException;
			} else if (Objects.nonNull(agentException)) {
				throw agentException;
			}
		}
	}

	@Override
	public OperationStatus execute(Operation operation) throws AbstractAgentException {
		this.operation = operation;
		executed = true;
		if (ExceptionLocation.EXECUTE == location) {
			if (Objects.nonNull(runtimeException)) {
				throw runtimeException;
			} else if (Objects.nonNull(agentException)) {
				throw agentException;
			}
		}
		return operationStatus;
	}

	@Override
	public String[] getSupportedOperations() {
		return new String[] { "c8y_Test" };
	}

	public boolean isStarted() {
		return started;
	}

	public boolean isStopped() {
		return stopped;
	}

	public boolean isExecuted() {
		return executed;
	}

	public Operation getOperation() {
		return operation;
	}

}
