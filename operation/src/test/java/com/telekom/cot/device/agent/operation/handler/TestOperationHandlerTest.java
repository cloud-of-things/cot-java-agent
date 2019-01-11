package com.telekom.cot.device.agent.operation.handler;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.exc.OperationHandlerServiceException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.operation.operations.TestOperation;
import com.telekom.cot.device.agent.operation.operations.TestOperation.GivenStatus;
import com.telekom.cot.device.agent.platform.objects.operation.Operation.OperationStatus;

public class TestOperationHandlerTest {

    private static final String OPERATION_ID = "12345";
    
    @Mock
    private Logger mockLogger;
    
    private TestOperationHandler handler;
    private TestOperationConfiguration configuration = new TestOperationConfiguration();
    private TestOperation operation;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        handler = new TestOperationHandler();
        
        InjectionUtil.injectStatic(TestOperationHandler.class, mockLogger);
        InjectionUtil.inject(handler, configuration);
        
        configuration.setDelay(1);

        operation = new TestOperation();
        operation.setId(OPERATION_ID);
    }
    
    @Test
    public void testGetSupportedOperationType() {
        assertSame(TestOperation.class, handler.getSupportedOperationType());
    }
    
    
    @Test(expected=OperationHandlerServiceException.class)
    public void testExecuteNullOperation() throws Exception {
        handler.execute(null);
    }

    @Test(expected=OperationHandlerServiceException.class)
    public void testExecuteNoConfiguration() throws Exception {
        InjectionUtil.inject(handler, "configuration", null);
        handler.execute(operation);
    }
    
    @Test(expected=OperationHandlerServiceException.class)
    public void testExecuteNoGivenStatus() throws Exception {
        handler.execute(operation);
    }

    @Test
    public void testExecuteGivenStatusSuccessful() throws Exception {
        operation.setGivenStatus(GivenStatus.GIVEN_SUCCESSFUL);
        assertEquals(OperationStatus.SUCCESSFUL, handler.execute(operation));
    }

    @Test
    public void testExecuteGivenStatusFailedByStatus() throws Exception {
        operation.setGivenStatus(GivenStatus.GIVEN_FAILED_BY_STATUS);
        assertEquals(OperationStatus.FAILED, handler.execute(operation));
    }

    @Test(expected=OperationHandlerServiceException.class)
    public void testExecuteGivenStatusFailedByException() throws Exception {
        operation.setGivenStatus(GivenStatus.GIVEN_FAILED_BY_EXCEPTION);
        handler.execute(operation);
    }
}
