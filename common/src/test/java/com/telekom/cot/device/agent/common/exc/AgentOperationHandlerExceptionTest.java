package com.telekom.cot.device.agent.common.exc;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class AgentOperationHandlerExceptionTest {

    @Test
    public void test(){
        AgentOperationHandlerException exception = new AgentOperationHandlerException("message");
        Assert.assertThat(exception.getMessage(), Matchers.equalTo("message"));

        exception = new AgentOperationHandlerException("message", new Exception());
        Assert.assertThat(exception.getMessage(), Matchers.equalTo("message"));
    }
}
