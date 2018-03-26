package com.telekom.cot.device.agent.common.exc;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class AgentCredentialsNotFoundExceptionTest {

    @Test
    public void test(){
        AgentCredentialsNotFoundException exception = new AgentCredentialsNotFoundException("message");
        String message = exception.getMessage();
        Assert.assertThat(message, Matchers.equalTo("message"));


        exception = new AgentCredentialsNotFoundException("message", new Exception());
        Assert.assertThat(message, Matchers.equalTo("message"));

        exception = new AgentCredentialsNotFoundException("message", new Exception(), true, true);
        Assert.assertThat(message, Matchers.equalTo("message"));
    }
}
