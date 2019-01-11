package com.telekom.cot.device.agent.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.exc.OperationHandlerServiceException;
import com.telekom.cot.device.agent.common.exc.OperationServiceException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.operation.handler.OperationHandlerService;
import com.telekom.cot.device.agent.operation.operations.RestartOperation;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.platform.objects.operation.Operation;
import com.telekom.cot.device.agent.platform.objects.operation.Operation.OperationStatus;

public class OperationWorkerTest {

	private static final String RESTART_OPERATION_NAME = "c8y_Restart";

	@Mock
	Logger mockLogger;
	@Mock
	PlatformService mockPlatformService;
	@SuppressWarnings("rawtypes")
    @Mock
	OperationHandlerService mockHandlerService;

	private Operation operation;
	@SuppressWarnings("rawtypes")
    private List<OperationHandlerService> operationHandlerServices;
    private boolean requestedNextPendingOperationAlready;
	
	/** class to test */
	private OperationWorker operationWorker;

	@SuppressWarnings({ "unchecked", "rawtypes" })
    @Before
	public void setUp() throws Exception {
		// initialize
		MockitoAnnotations.initMocks(this);

		// initialize operation
		operation = new Operation("123") {};
		operation.setStatus(OperationStatus.PENDING);
        operation.setProperty(RESTART_OPERATION_NAME, "test");

        // behavior of PlatformService.getNextPendingOperation : return operation once
        requestedNextPendingOperationAlready = false;
        doAnswer(new Answer<Operation>() {
            @Override
            public Operation answer(InvocationOnMock invocation) throws Throwable {
                // return 'operationPending' only at first time
                if (requestedNextPendingOperationAlready) {
                    return null;
                }
                
                requestedNextPendingOperationAlready = true;
                return operation;
            }
        }).when(mockPlatformService).getNextPendingOperation();
		
		// initialize handler map
		operationHandlerServices = new ArrayList<>();
		operationHandlerServices.add(mockHandlerService);

		// behavior of mocked operation handler service
		when(mockHandlerService.getSupportedOperationType()).thenReturn((Class) RestartOperation.class);
        when(mockHandlerService.execute(any(RestartOperation.class))).thenReturn(OperationStatus.SUCCESSFUL);

		// initialize class to test and inject mocks
		operationWorker = new OperationWorker(mockPlatformService, operationHandlerServices, 1);
		InjectionUtil.injectStatic(OperationWorker.class, mockLogger);
	}

	@Test
	public void testNoPendingOperations() throws Exception {
		doReturn(null).when(mockPlatformService).getNextPendingOperation();

		operationWorker.start();
		assertTrue(operationWorker.isStarted());
		TimeUnit.MILLISECONDS.sleep(100);
		operationWorker.stop();
		assertTrue(operationWorker.isStopped());

        verify(mockPlatformService, never()).updateOperationStatus(any(String.class), any(OperationStatus.class));
        verify(mockLogger, atLeast(1)).debug("no pending operation found");
	}

    @Test
    public void testGetNextPendingOperationException() throws Exception {
        Exception e = new PlatformServiceException("test");
        doThrow(e).when(mockPlatformService).getNextPendingOperation();

        operationWorker.start();
        assertTrue(operationWorker.isStarted());
        TimeUnit.MILLISECONDS.sleep(100);
        operationWorker.stop();
        assertTrue(operationWorker.isStopped());

        verify(mockLogger, times(1)).debug("no pending operation found");
        verify(mockPlatformService, never()).updateOperationStatus(any(String.class), any(OperationStatus.class));
    }

    @Test
    public void testNotConvertableOperation() throws Exception {
        operation.removeProperty(RESTART_OPERATION_NAME);
        operation.setProperty("c8y_NotExistingOperation", "test");

        operationWorker.start();
        assertTrue(operationWorker.isStarted());
        TimeUnit.MILLISECONDS.sleep(100);
        operationWorker.stop();
        assertTrue(operationWorker.isStopped());
        
        OperationStatus expectedStatus = OperationStatus.FAILED;
        assertEquals(expectedStatus, operation.getStatus());
        verify(mockPlatformService, never()).updateOperationStatus(any(String.class), eq(OperationStatus.EXECUTING));
        verify(mockPlatformService, times(1)).updateOperationStatus(operation.getId(), expectedStatus);
        verify(mockLogger, times(1)).error("can't convert operation to specific operation type");
    }

	@Test
	public void testOperationExecutionSuccessful() throws Exception {
		operationWorker.start();
		assertTrue(operationWorker.isStarted());
		TimeUnit.MILLISECONDS.sleep(100);
		operationWorker.stop();
		assertTrue(operationWorker.isStopped());
		
        OperationStatus expectedStatus = OperationStatus.SUCCESSFUL;
        verify(mockPlatformService, atLeastOnce()).getNextPendingOperation();
        verify(mockPlatformService, times(1)).updateOperationStatus(operation.getId(), OperationStatus.EXECUTING);
        verify(mockPlatformService, times(1)).updateOperationStatus(operation.getId(), expectedStatus);
        verify(mockLogger, times(1)).debug("handled operation, status is {}", expectedStatus);
	}

    @SuppressWarnings("unchecked")
    @Test
    public void testOperationExecutionFailed() throws Exception {
        doReturn(OperationStatus.FAILED).when(mockHandlerService).execute(any(RestartOperation.class));

        operationWorker.start();
        assertTrue(operationWorker.isStarted());
        TimeUnit.MILLISECONDS.sleep(100);
        operationWorker.stop();
        assertTrue(operationWorker.isStopped());

        OperationStatus expectedStatus = OperationStatus.FAILED;
        verify(mockPlatformService, atLeastOnce()).getNextPendingOperation();
        verify(mockPlatformService, times(1)).updateOperationStatus(operation.getId(), OperationStatus.EXECUTING);
        verify(mockPlatformService, times(1)).updateOperationStatus(operation.getId(), expectedStatus);
        verify(mockLogger, times(1)).debug("handled operation, status is {}", expectedStatus);
    }

	@SuppressWarnings("unchecked")
    @Test
	public void testHandlerServiceException() throws Exception {
		Exception e = new OperationHandlerServiceException("test");
		doThrow(e).when(mockHandlerService).execute(any(RestartOperation.class));

		operationWorker.start();
		assertTrue(operationWorker.isStarted());
		TimeUnit.MILLISECONDS.sleep(100);
        operationWorker.stop();
        assertTrue(operationWorker.isStopped());

        OperationStatus expectedStatus = OperationStatus.FAILED;
        verify(mockPlatformService, times(1)).updateOperationStatus(operation.getId(), OperationStatus.EXECUTING);
        verify(mockPlatformService, times(1)).updateOperationStatus(operation.getId(), expectedStatus);
        verify(mockLogger, times(1)).error("can't execute operation", e);
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
		operationWorker = new OperationWorker(mockPlatformService, operationHandlerServices, -1);

		operationWorker.start();
        TimeUnit.MILLISECONDS.sleep(2000);
		operationWorker.stop();
	}
}
