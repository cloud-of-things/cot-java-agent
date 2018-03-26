package com.telekom.cot.device.agent.common.exc;

import org.junit.Assert;
import org.junit.Test;

public class ConfigurationUpdateExceptionTest {

	@Test
	public void test() {
		ConfigurationUpdateException configurationUpdateException = new ConfigurationUpdateException("testMessage");
		Assert.assertEquals("testMessage", configurationUpdateException.getMessage());

		configurationUpdateException = new ConfigurationUpdateException("testMessage", new Throwable());
		Assert.assertEquals("testMessage", configurationUpdateException.getMessage());

		configurationUpdateException = new ConfigurationUpdateException("testMessage", new Throwable(), true, true);
		Assert.assertEquals("testMessage", configurationUpdateException.getMessage());
	}
}
