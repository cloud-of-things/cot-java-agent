package com.telekom.cot.device.agent.common.exc;

import org.junit.Assert;
import org.junit.Test;

public class SystemServiceExceptionTest {

	@Test
	public void test() {
		SystemServiceException systemServiceException = new SystemServiceException("testMessage");
		Assert.assertEquals("testMessage", systemServiceException.getMessage());

		systemServiceException = new SystemServiceException("testMessage", new Throwable());
		Assert.assertEquals("testMessage", systemServiceException.getMessage());

		systemServiceException = new SystemServiceException("testMessage", new Throwable(), true, true);
		Assert.assertEquals("testMessage", systemServiceException.getMessage());
	}
}