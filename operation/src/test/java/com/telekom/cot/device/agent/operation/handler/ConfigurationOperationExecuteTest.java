package com.telekom.cot.device.agent.operation.handler;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentOperationHandlerException;
import com.telekom.cot.device.agent.platform.objects.Operation;
import com.telekom.cot.device.agent.platform.objects.OperationStatus;

public class ConfigurationOperationExecuteTest {

    private OperationExecuteCallback<String> callback;
    private OperationExecuteCallback<String> errorCallback;
    private String callbackCcontent;
    
    @Mock
    OperationExecuteBuilder mockOperationExecuteBuilder;

    @Before
    public void setUp() {
        callbackCcontent = null;
        callback = new OperationExecuteCallback<String>() {
            @Override
            public void finished(String content) throws AbstractAgentException {
                callbackCcontent = content;
            }
        };
        errorCallback = new OperationExecuteCallback<String>() {
            @Override
            public void finished(String content) throws AbstractAgentException {
                throw new AgentOperationHandlerException("test");
            }
        };
    }

    @Test
    public void testExecute_SUCCESSFUL() throws AbstractAgentException {

        Operation operation = new Operation();
        Map<String, Object> confOperation = new HashMap<String, Object>();
        confOperation.put("config", "agent.yaml");
        operation.setProperty("c8y_Configuration", confOperation);

        OperationStatus status = OperationExecuteBuilder.create(operation) //
                .setExecutorClass(ConfigurationOperationExecute.class) //
                .setCallback(callback).build() //
                .perform();

        Assert.assertThat(callbackCcontent, Matchers.equalTo("agent.yaml"));
        Assert.assertThat(status, Matchers.equalTo(OperationStatus.SUCCESSFUL));

    }

    @Test(expected = AbstractAgentException.class)
    public void testExecuteErrorCallback() throws AbstractAgentException {

        Operation operation = new Operation();
        Map<String, Object> confOperation = new HashMap<String, Object>();
        confOperation.put("config", "agent.yaml");
        operation.setProperty("c8y_Configuration", confOperation);

        OperationExecuteBuilder.create(operation) //
                .setExecutorClass(ConfigurationOperationExecute.class) //
                .setCallback(errorCallback).build() //
                .perform();

    }
    
    /**
     * Test missing config in operation
     * @throws AbstractAgentException
     */
    @Test(expected = AgentOperationHandlerException.class)
    public void testExecuteMissingC8y_Configuration() throws AbstractAgentException {

        Operation operation = new Operation();

        OperationExecuteBuilder.create(operation) //
                .setExecutorClass(ConfigurationOperationExecute.class) //
                .setCallback(callback).build() //
                .perform();
    }
    
    /**
     * Test missing config in operation
     * @throws AbstractAgentException
     */
    @Test(expected = AgentOperationHandlerException.class)
    public void testExecuteConvertStringObject() throws AbstractAgentException {

        Operation operation = new Operation();
        Map<String, Object> confOperation = new HashMap<String, Object>();
        confOperation.put("config", new Object());
        operation.setProperty("c8y_Configuration", confOperation);

        OperationExecuteBuilder.create(operation) //
                .setExecutorClass(ConfigurationOperationExecute.class) //
                .setCallback(errorCallback).build() //
                .perform();
    }
}
