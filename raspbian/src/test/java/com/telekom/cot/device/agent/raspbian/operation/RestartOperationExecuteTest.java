package com.telekom.cot.device.agent.raspbian.operation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.util.InjectionUtil;
import com.telekom.cot.device.agent.operation.handler.OperationExecuteBuilder;
import com.telekom.cot.device.agent.raspbian.operation.RestartOperationExecute;
import com.telekom.cot.device.agent.operation.handler.OperationExecute;
import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;

public class RestartOperationExecuteTest {

    private static final String RESTART_COMMAND = "sudo reboot &";

    @SuppressWarnings("rawtypes")
    private OperationExecute restartOperation;

    private Runtime mockRuntime;

    @Before
    public void setUp() throws AbstractAgentException {

        restartOperation = OperationExecuteBuilder.create(new Operation()).setExecutorClass(RestartOperationExecute.class).build();
        mockRuntime = mock(Runtime.class);

        InjectionUtil.inject(restartOperation, mockRuntime);
        InjectionUtil.inject(restartOperation, "isLinuxOs", Boolean.TRUE);
    }

    @Test
    public void testExecute() throws AbstractAgentException, IOException {
        assertEquals(OperationStatus.EXECUTING, restartOperation.perform());
        verify(mockRuntime).exec(RESTART_COMMAND);
    }

    @Test(expected = AbstractAgentException.class)
    public void testExecuteNoLinux() throws AbstractAgentException, IOException {
        InjectionUtil.inject(restartOperation, "isLinuxOs", Boolean.FALSE);

        restartOperation.perform();
    }

    @Test
    public void testExecuteExecException() throws AbstractAgentException, IOException {
        doThrow(new IOException()).when(mockRuntime).exec(RESTART_COMMAND);
        
        assertEquals(OperationStatus.FAILED, restartOperation.perform());
        verify(mockRuntime).exec(RESTART_COMMAND);
    }
}
