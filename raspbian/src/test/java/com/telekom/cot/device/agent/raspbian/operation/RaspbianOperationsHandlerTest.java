package com.telekom.cot.device.agent.raspbian.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentOperationHandlerException;
import com.telekom.cot.device.agent.operation.handler.OperationExecute;
import com.telekom.cot.device.agent.operation.handler.OperationExecuteBuilder;
import com.telekom.cot.device.agent.platform.objects.Operation;
import com.telekom.cot.device.agent.platform.objects.OperationStatus;

@RunWith(PowerMockRunner.class)
@PrepareForTest(OperationExecuteBuilder.class)
public class RaspbianOperationsHandlerTest {

    private Operation operation;
    private RaspbianOperationsHandler handler;
    @Mock
    private OperationExecuteBuilder mockOperationExecuteBuilder;
    @SuppressWarnings("rawtypes")
    @Mock
    private OperationExecute mockOperationExecute;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws AbstractAgentException {

        operation = new Operation();
        operation.setProperty("c8y_Restart", new HashMap<String, Object>());

        handler = new RaspbianOperationsHandler();

        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(OperationExecuteBuilder.class);
        PowerMockito.when(OperationExecuteBuilder.create(Mockito.any())).thenReturn(mockOperationExecuteBuilder);

        when(mockOperationExecuteBuilder.setExecutorClass(Mockito.any())).thenReturn(mockOperationExecuteBuilder);
        when(mockOperationExecuteBuilder.addParameter(Mockito.any(), Mockito.any())).thenReturn(mockOperationExecuteBuilder);
        when(mockOperationExecuteBuilder.setParameters(Mockito.any())).thenReturn(mockOperationExecuteBuilder);
        when(mockOperationExecuteBuilder.build()).thenReturn(mockOperationExecute);

    }

    @Test
    public void testGetSupportedOperations() {
        String[] supportedOperations = handler.getSupportedOperations();
        assertNotNull(supportedOperations);
        assertEquals(1, supportedOperations.length);
        assertEquals("c8y_Restart", supportedOperations[0]);
    }
    
    @Test(expected=AgentOperationHandlerException.class)
    public void testExecuteNoOperation() throws Exception {
        handler.execute(null);
    }
    
    @Test(expected=AgentOperationHandlerException.class)
    public void testExecuteOperationNoAttributes() throws Exception {
        handler.execute(new Operation());
    }
    
    @Test(expected=AgentOperationHandlerException.class)
    public void testExecuteNotSupportedOperation() throws Exception {
        operation = new Operation();
        operation.setProperty("c8y_Test", new HashMap<String, Object>());
        handler.execute(operation);
    }
    
    @Test(expected=AgentOperationHandlerException.class)
    public void testExecuteException() throws AbstractAgentException {
        // given perform exception
        when(mockOperationExecute.perform()).thenThrow(new AgentOperationHandlerException("test"));

        // when execute by handler
        handler.execute(operation);
    }

    @Test
    public void testExecuteFailed() throws AbstractAgentException {
        // given perform status FAILED
        when(mockOperationExecute.perform()).thenReturn(OperationStatus.FAILED);

        // when execute by handler than status is FAILED
        assertEquals(OperationStatus.FAILED, handler.execute(operation));
    }

    @Test
    public void testExecuteSuccessful() throws AbstractAgentException {
        // given perform status SUCCESSFUL
        when(mockOperationExecute.perform()).thenReturn(OperationStatus.SUCCESSFUL);
        // when execute by handler than status is SUCCESSFUL
        assertEquals(OperationStatus.SUCCESSFUL, handler.execute(operation));
    }
}
