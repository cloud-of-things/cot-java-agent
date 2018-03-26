package com.telekom.cot.device.agent.common.exc;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class ConfigurationNotFoundExceptionTest {
    @Test
    public void test(){
        ConfigurationNotFoundException exception = new ConfigurationNotFoundException("message");
        String message = exception.getMessage();
        Assert.assertThat(message, Matchers.equalTo("message"));


        exception = new ConfigurationNotFoundException("message", new Exception());
        Assert.assertThat(message, Matchers.equalTo("message"));

        exception = new ConfigurationNotFoundException("message", new Exception(), true, true);
        Assert.assertThat(message, Matchers.equalTo("message"));
    }
}
