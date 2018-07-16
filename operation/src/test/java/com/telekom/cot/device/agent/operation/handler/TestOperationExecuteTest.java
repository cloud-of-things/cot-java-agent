package com.telekom.cot.device.agent.operation.handler;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.platform.objects.Operation;
import com.telekom.cot.device.agent.platform.objects.OperationStatus;

public class TestOperationExecuteTest {

    @Test
    public void testExecute_SUCCESSFUL() throws AbstractAgentException {
        Operation operation = new Operation();
        Map<String, Object> testOperation = new HashMap<String, Object>();
        testOperation.put("givenStatus", "GIVEN_SUCCESSFUL");
        operation.setProperty("c8y_TestOperation", testOperation);

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
        Map<String, Object> testOperation = new HashMap<String, Object>();
        testOperation.put("givenStatus", "GIVEN_FAILED_BY_STATUS");
        operation.setProperty("c8y_TestOperation", testOperation);

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
        Map<String, Object> testOperation = new HashMap<String, Object>();
        testOperation.put("givenStatus", "GIVEN_FAILED_BY_EXCEPTION");
        operation.setProperty("c8y_TestOperation", testOperation);

        TestOperationConfig configuration = new TestOperationConfig();
        configuration.setDelay(1);

        OperationExecuteBuilder.create(operation) //
                .setExecutorClass(TestOperationExecute.class) //
                .setConfiguration(configuration).build() //
                .perform();

    }
    
    @Test(expected = AbstractAgentException.class)
    public void testExecute_UNKNOWN() throws AbstractAgentException {

        Operation operation = new Operation();
        Map<String, Object> testOperation = new HashMap<String, Object>();
        testOperation.put("givenStatus", "");
        operation.setProperty("c8y_TestOperation", testOperation);

        TestOperationConfig configuration = new TestOperationConfig();
        configuration.setDelay(1);

        OperationExecuteBuilder.create(operation) //
                .setExecutorClass(TestOperationExecute.class) //
                .setConfiguration(configuration).build() //
                .perform();

    }
}
