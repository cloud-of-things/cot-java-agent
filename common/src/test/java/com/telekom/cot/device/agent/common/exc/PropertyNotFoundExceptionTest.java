package com.telekom.cot.device.agent.common.exc;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class PropertyNotFoundExceptionTest {

	@Test
	public void test() {

		PropertyNotFoundException exc = new PropertyNotFoundException("message");
		Assert.assertThat(exc.getMessage(), Matchers.equalTo("message"));

	}

}
