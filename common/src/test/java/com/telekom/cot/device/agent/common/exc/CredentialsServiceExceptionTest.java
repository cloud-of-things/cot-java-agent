package com.telekom.cot.device.agent.common.exc;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class CredentialsServiceExceptionTest {
    @Test
    public void test(){
        CredentialsServiceException exception = new CredentialsServiceException("message");
        String message = exception.getMessage();
        Assert.assertThat(message, Matchers.equalTo("message"));


        exception = new CredentialsServiceException("message", new Exception());
        Assert.assertThat(message, Matchers.equalTo("message"));

        exception = new CredentialsServiceException("message", new Exception(), true, true);
        Assert.assertThat(message, Matchers.equalTo("message"));
    }
}
