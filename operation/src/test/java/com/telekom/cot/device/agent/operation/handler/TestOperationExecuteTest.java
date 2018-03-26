package com.telekom.cot.device.agent.operation.handler;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.operation.handler.TestOperationExecute;
import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;
import com.telekom.m2m.cot.restsdk.util.ExtensibleObject;

public class TestOperationExecuteTest {

    @Test
    public void testExecute_SUCCESSFUL() throws AbstractAgentException {

        Operation operation = new Operation();
        ExtensibleObject testOperation = new ExtensibleObject();
        testOperation.set("givenStatus", "GIVEN_SUCCESSFUL");
        operation.set("c8y_TestOperation", testOperation);

        TestOperationConfig configuration = new TestOperationConfig();
        configuration.setDelay(1);

        OperationStatus status = OperationExecuteBuilder.create(operation) //
                .setExecutorClass(TestOperationExecute.class) //
                .setConfiguration(configuration).build() //
                .perform();

        Assert.assertThat(status, Matchers.equalTo(OperationStatus.SUCCESSFUL));

    }

    @Test
    public void testExecute_FAILED_BY_STATUS() throws AbstractAgentException {

        Operation operation = new Operation();
        ExtensibleObject testOperation = new ExtensibleObject();
        testOperation.set("givenStatus", "GIVEN_FAILED_BY_STATUS");
        operation.set("c8y_TestOperation", testOperation);

        TestOperationConfig configuration = new TestOperationConfig();
        configuration.setDelay(1);

        OperationStatus status = OperationExecuteBuilder.create(operation) //
                .setExecutorClass(TestOperationExecute.class) //
                .setConfiguration(configuration).build() //
                .perform();

        Assert.assertThat(status, Matchers.equalTo(OperationStatus.FAILED));

    }

    @Test(expected = AbstractAgentException.class)
    public void testExecute_FAILED_BY_EXCEPTION() throws AbstractAgentException {

        Operation operation = new Operation();
        ExtensibleObject testOperation = new ExtensibleObject();
        testOperation.set("givenStatus", "GIVEN_FAILED_BY_EXCEPTION");
        operation.set("c8y_TestOperation", testOperation);

        TestOperationConfig configuration = new TestOperationConfig();
        configuration.setDelay(1);

        OperationExecuteBuilder.create(operation) //
                .setExecutorClass(TestOperationExecute.class) //
                .setConfiguration(configuration).build() //
                .perform();

    }

}
