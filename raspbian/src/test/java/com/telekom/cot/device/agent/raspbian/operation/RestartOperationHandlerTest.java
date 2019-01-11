package com.telekom.cot.device.agent.raspbian.operation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.telekom.cot.device.agent.common.exc.OperationHandlerServiceException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.operation.operations.RestartOperation;
import com.telekom.cot.device.agent.platform.objects.operation.Operation.OperationStatus;


public class RestartOperationHandlerTest {

    @Mock
    private Runtime mockRuntime;
    
    private RestartOperationHandler handler;
    private RestartOperation operation = new RestartOperation();
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        handler = new RestartOperationHandler();
        
        InjectionUtil.inject(handler, mockRuntime);
        InjectionUtil.inject(handler, "isLinuxOs", true);
    }
    
    @Test
    public void testStart() throws Exception {
        handler.start();
    }
    
    @Test
    public void testGetSupportedOperationType() {
        assertSame(RestartOperation.class, handler.getSupportedOperationType());
    }

    @Test(expected=OperationHandlerServiceException.class)
    public void testExecuteNull() throws Exception {
        handler.execute(null);
    }

    @Test(expected=OperationHandlerServiceException.class)
    public void testExecuteNoLinuxSystem() throws Exception {
        InjectionUtil.inject(handler, "isLinuxOs", false);
        handler.execute(operation);
    }

    @Test
    public void testExecuteRuntimeExecException() throws Exception {
        doThrow(new IOException()).when(mockRuntime).exec(any(String.class));
        assertEquals(OperationStatus.FAILED, handler.execute(operation));
    }

    @Test
    public void testExecuteRuntime() throws Exception {
        assertEquals(OperationStatus.EXECUTING, handler.execute(operation));
    }
}
