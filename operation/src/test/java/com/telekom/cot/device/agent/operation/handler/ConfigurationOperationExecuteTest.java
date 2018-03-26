package com.telekom.cot.device.agent.operation.handler;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentOperationHandlerException;
import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;
import com.telekom.m2m.cot.restsdk.util.ExtensibleObject;

public class ConfigurationOperationExecuteTest {

    private OperationExecuteCallback<String> callback;
    private OperationExecuteCallback<String> errorCallback;
    private String callbackCcontent;

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
        ExtensibleObject confOperation = new ExtensibleObject();
        confOperation.set("config", "agent.yaml");
        operation.set("c8y_Configuration", confOperation);

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
        ExtensibleObject confOperation = new ExtensibleObject();
        confOperation.set("config", "agent.yaml");
        operation.set("c8y_Configuration", confOperation);

        OperationExecuteBuilder.create(operation) //
                .setExecutorClass(ConfigurationOperationExecute.class) //
                .setCallback(errorCallback).build() //
                .perform();

    }

}
