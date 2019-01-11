package com.telekom.cot.device.agent.common.exc;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class OperationHandlerServiceExceptionTest {

    @Test
    public void test(){
        OperationHandlerServiceException exception = new OperationHandlerServiceException("message");
        Assert.assertThat(exception.getMessage(), Matchers.equalTo("message"));

        exception = new OperationHandlerServiceException("message", new Exception());
        Assert.assertThat(exception.getMessage(), Matchers.equalTo("message"));
    }
}
