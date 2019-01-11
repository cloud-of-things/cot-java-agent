package com.telekom.cot.device.agent.common.exc;

import org.junit.Assert;
import org.junit.Test;

public class MeasurementServiceExceptionTest {

	@Test
	public void test() {
		MeasurementServiceException measurementServiceException = new MeasurementServiceException("testMessage");
		Assert.assertEquals("testMessage", measurementServiceException.getMessage());

		measurementServiceException = new MeasurementServiceException("testMessage", new Throwable());
		Assert.assertEquals("testMessage", measurementServiceException.getMessage());

		measurementServiceException = new MeasurementServiceException("testMessage", new Throwable(), true, true);
		Assert.assertEquals("testMessage", measurementServiceException.getMessage());
	}
}
