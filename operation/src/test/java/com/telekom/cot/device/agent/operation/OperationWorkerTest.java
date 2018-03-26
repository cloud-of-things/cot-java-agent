package com.telekom.cot.device.agent.operation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentOperationHandlerException;
import com.telekom.cot.device.agent.common.exc.OperationServiceException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.common.util.InjectionUtil;
import com.telekom.cot.device.agent.inventory.InventoryService;
import com.telekom.cot.device.agent.operation.handler.OperationsHandlerService;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationCollection;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;
import com.telekom.m2m.cot.restsdk.devicecontrol.Restart;

public class OperationWorkerTest {

	private static final String RESTART_OPERATION_NAME = "c8y_Restart";

	@Mock
	Logger mockLogger;
	@Mock
	OperationCollection mockOperationCollection;
	@Mock
	PlatformService mockPlatformService;
	@Mock
	OperationsHandlerService mockHandlerService;
	@Mock
	InventoryService mockInventoryService;

	// private ExternalId externalId = new ExternalId();
	private Operation operation;
	private List<OperationsHandlerService> operationHandlerServices;

	/** class to test */
	private OperationWorker operationWorker;

	@Before
	public void setUp() throws Exception {
		// initialize
		MockitoAnnotations.initMocks(this);

		// initialize operation
		operation = new Operation();
		operation.setStatus(OperationStatus.PENDING);
		operation.set(RESTART_OPERATION_NAME, new Restart(RESTART_OPERATION_NAME));

		// initialize handler map
		operationHandlerServices = new ArrayList<>();
		operationHandlerServices.add(mockHandlerService);

		// behavior of mocked platform service
		when(mockPlatformService.getOperationCollection(OperationStatus.PENDING, 1)).thenReturn(mockOperationCollection,
				(OperationCollection) null);

		// behavior of mocked operation collection
		when(mockOperationCollection.hasNext()).thenReturn(false);
		when(mockOperationCollection.getOperations()).thenReturn(new Operation[] { operation });

		// behavior of mocked operation handler service
		when(mockHandlerService.getSupportedOperations()).thenReturn(new String[] { RESTART_OPERATION_NAME });
		when(mockHandlerService.execute(operation)).thenReturn(OperationStatus.SUCCESSFUL);
		when(mockHandlerService.getSupportedOperations()).thenReturn(new String[] { RESTART_OPERATION_NAME });

		// initialize class to test and inject mocks
		operationWorker = new OperationWorker(mockPlatformService, operationHandlerServices, 1, 1);
		InjectionUtil.injectStatic(OperationWorker.class, mockLogger);
	}

	@Test
	public void testStartAndStopWithoutOperations() throws Exception {
		reset(mockOperationCollection);
		when(mockOperationCollection.getOperations()).thenReturn(new Operation[] {});

		/**
		 * start and stop worker
		 */
		operationWorker.start();
		assertTrue(operationWorker.isStarted());

		operationWorker.stop();
		assertTrue(operationWorker.isStopped());
	}

	@Test
	public void testStartAndStopWithOperations_SUCCESSFUL() throws AbstractAgentException, InterruptedException {
		operationWorker.start();
		Thread.sleep(100);
		operationWorker.stop();

		verify(mockPlatformService, atLeastOnce()).getOperationCollection(OperationStatus.PENDING, 1);
		verify(mockPlatformService, atLeast(2)).updateOperation(operation);
		assertEquals(OperationStatus.SUCCESSFUL, operation.getStatus());

	}

	@Test
	public void testStartAndStopWithOperations_FAILED() throws AbstractAgentException, InterruptedException {
		reset(mockHandlerService);
		when(mockHandlerService.execute(operation)).thenReturn(OperationStatus.FAILED);

		operationWorker.start();
		Thread.sleep(100);
		operationWorker.stop();

		verify(mockPlatformService, atLeastOnce()).getOperationCollection(OperationStatus.PENDING, 1);
		verify(mockPlatformService, atLeast(2)).updateOperation(operation);
		assertEquals(OperationStatus.FAILED, operation.getStatus());
	}

	@Test
	public void testStartAndStopWithOperationsExecuteException() throws AbstractAgentException, InterruptedException {
	    AbstractAgentException exception = new AgentOperationHandlerException("can't handle"); 
		doThrow(exception).when(mockHandlerService).execute(operation);

		operationWorker.start();
		Thread.sleep(100);
		operationWorker.stop();

		verify(mockPlatformService, atLeastOnce()).getOperationCollection(OperationStatus.PENDING, 1);
		verify(mockLogger).error("can't execute operation", exception);
		verify(mockPlatformService, atLeastOnce()).updateOperation(operation);
		assertEquals(OperationStatus.FAILED, operation.getStatus());
	}

	@Test
	public void testStartAndStopWithOperationsNoHandler() throws AbstractAgentException, InterruptedException {

		Operation unknownOperation = new Operation();
		unknownOperation.setStatus(OperationStatus.PENDING);
		unknownOperation.set("c8y_unknown", new Restart("c8y_unknown"));
		Map<String, Object> operationAttributes = unknownOperation.getAttributes();
		
		reset(mockOperationCollection);
		when(mockOperationCollection.getOperations()).thenReturn(new Operation[] { unknownOperation });

		operationWorker.start();
		Thread.sleep(100);
		operationWorker.stop();

        verify(mockPlatformService, atLeastOnce()).getOperationCollection(OperationStatus.PENDING, 1);
		verify(mockLogger).debug("did not found operation handler {}", operationAttributes);
		verify(mockPlatformService).updateOperation(unknownOperation);
		assertEquals(OperationStatus.FAILED, unknownOperation.getStatus());
	}

	@Test
	public void testStartAndStopWithOperationsGetException() throws AbstractAgentException, InterruptedException {
		reset(mockPlatformService);
		doThrow(new PlatformServiceException("test")).when(mockPlatformService)
				.getOperationCollection(OperationStatus.PENDING, 1);

		operationWorker.start();
		Thread.sleep(100);
		operationWorker.stop();

		verify(mockPlatformService, times(1)).getOperationCollection(OperationStatus.PENDING, 1);
		verify(mockPlatformService, never()).updateOperation(any(Operation.class));
	}

	@Test(expected = OperationServiceException.class)
	public void testStartTwice() throws Exception {
		operationWorker.start();
		operationWorker.start();
		operationWorker.stop();
	}

	@Test
	public void testStopWithoutStart() throws Exception {
		operationWorker.stop();
	}

	@Test
	public void testNegativeInterval() throws Exception {
		operationWorker = new OperationWorker(mockPlatformService, operationHandlerServices, -1, 1);

		operationWorker.start();
		Thread.sleep(2000);
		operationWorker.stop();
	}
}
