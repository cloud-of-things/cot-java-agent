package com.telekom.cot.device.agent.common.exc;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class OperationServiceExceptionTest {
    @Test
    public void test(){
        OperationServiceException exception = new OperationServiceException("message");
        String message = exception.getMessage();
        Assert.assertThat(message, Matchers.equalTo("message"));


        exception = new OperationServiceException("message", new Exception());
        Assert.assertThat(message, Matchers.equalTo("message"));

        exception = new OperationServiceException("message", new Exception(), true, true);
        Assert.assertThat(message, Matchers.equalTo("message"));
    }
}
