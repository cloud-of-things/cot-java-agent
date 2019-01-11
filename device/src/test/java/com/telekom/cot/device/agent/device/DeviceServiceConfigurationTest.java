package com.telekom.cot.device.agent.device;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class DeviceServiceConfigurationTest {

	private static final Long handlersShutdownTimeout = 100L;
    private static final String toStringValue = DeviceServiceConfiguration.class.getSimpleName() + " [handlersShutdownTimeout=" + handlersShutdownTimeout + "]";

	
	private DeviceServiceConfiguration configuration;

	@Test
	public void testGettersAndSetters() {
		configuration = new DeviceServiceConfiguration();
		configuration.setHandlersShutdownTimeout(handlersShutdownTimeout);
	
		assertEquals(handlersShutdownTimeout, configuration.getHandlersShutdownTimeout());
	}
	
	@Test
	public void testToString() {
		configuration = new DeviceServiceConfiguration();
		configuration.setHandlersShutdownTimeout(handlersShutdownTimeout);
		
		assertEquals(toStringValue, configuration.toString());
	}
}
