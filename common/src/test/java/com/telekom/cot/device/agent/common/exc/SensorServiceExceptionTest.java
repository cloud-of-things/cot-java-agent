package com.telekom.cot.device.agent.common.exc;

import org.junit.Assert;
import org.junit.Test;

public class SensorServiceExceptionTest {

	@Test
	public void test() {
		SensorServiceException sensorServiceException = new SensorServiceException("testMessage");
		Assert.assertEquals("testMessage", sensorServiceException.getMessage());

		sensorServiceException = new SensorServiceException("testMessage", new Throwable());
		Assert.assertEquals("testMessage", sensorServiceException.getMessage());

		sensorServiceException = new SensorServiceException("testMessage", new Throwable(), true, true);
		Assert.assertEquals("testMessage", sensorServiceException.getMessage());
	}
}
