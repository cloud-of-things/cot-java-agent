package com.telekom.cot.device.agent.common.exc;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class AgentServiceNotFoundExceptionTest {

    @Test
    public void test(){
        AgentServiceNotFoundException exception = new AgentServiceNotFoundException("message");
        Assert.assertThat(exception.getMessage(), Matchers.equalTo("message"));
    }

}
