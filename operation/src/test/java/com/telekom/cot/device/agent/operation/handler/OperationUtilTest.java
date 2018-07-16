package com.telekom.cot.device.agent.operation.handler;

import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.operation.handler.AgentOperationsHandlerService.OperationType;
import com.telekom.cot.device.agent.operation.handler.TestOperationExecute.GivenStatus;
import com.telekom.cot.device.agent.platform.objects.Operation;

public class OperationUtilTest {

    @Test
    public void testStatusGIVEN_SUCCESSFUL() throws AbstractAgentException {
        Operation operation = new Operation();
        Map<String, Object> values = new HashMap<>();
        values.put("givenStatus", String.valueOf(TestOperationExecute.GivenStatus.GIVEN_SUCCESSFUL));
        operation.setProperty(OperationType.C8Y_TEST_OPERATION.getAttribute(), values);
        GivenStatus status = OperationUtil.getGivenStatus(operation);
        assertThat(status, Matchers.equalTo(GivenStatus.GIVEN_SUCCESSFUL));
    }

    @Test(expected = AbstractAgentException.class)
    public void testStatusError() throws AbstractAgentException {
        Operation operation = new Operation();
        Map<String, Object> values = new HashMap<>();
        values.put("givenStatus", "NOT_EXIST");
        operation.setProperty(OperationType.C8Y_TEST_OPERATION.getAttribute(), values);
        OperationUtil.getGivenStatus(operation);
    }
}
