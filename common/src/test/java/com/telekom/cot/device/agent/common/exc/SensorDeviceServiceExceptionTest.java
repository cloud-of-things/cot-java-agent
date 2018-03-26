package com.telekom.cot.device.agent.common.exc;

import org.junit.Assert;
import org.junit.Test;

public class SensorDeviceServiceExceptionTest {

	@Test
	public void test() {
		SensorDeviceServiceException sensorDeviceServiceException = new SensorDeviceServiceException("testMessage");
		Assert.assertEquals("testMessage", sensorDeviceServiceException.getMessage());

		sensorDeviceServiceException = new SensorDeviceServiceException("testMessage", new Throwable());
		Assert.assertEquals("testMessage", sensorDeviceServiceException.getMessage());

		sensorDeviceServiceException = new SensorDeviceServiceException("testMessage", new Throwable(), true, true);
		Assert.assertEquals("testMessage", sensorDeviceServiceException.getMessage());
	}
}
