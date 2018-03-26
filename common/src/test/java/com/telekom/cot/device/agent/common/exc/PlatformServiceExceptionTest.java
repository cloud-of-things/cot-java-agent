package com.telekom.cot.device.agent.common.exc;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class PlatformServiceExceptionTest {

	@Test
	public void test() {

		PlatformServiceException exc = new PlatformServiceException("message", new Exception());
		String message = exc.getMessage();
		Assert.assertThat(message, Matchers.equalTo("message"));

		exc = new PlatformServiceException(500, "message", new Exception());
		message = exc.getMessage();

		Assert.assertThat(message, Matchers.equalTo("message"));
		Assert.assertThat(exc.getHttpStatus(), Matchers.equalTo(500));

		exc = new PlatformServiceException("message");
		message = exc.getMessage();

		Assert.assertThat(message, Matchers.equalTo("message"));
	}

}
