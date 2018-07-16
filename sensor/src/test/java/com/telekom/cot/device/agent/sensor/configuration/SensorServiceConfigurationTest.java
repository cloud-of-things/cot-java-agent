package com.telekom.cot.device.agent.sensor.configuration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SensorServiceConfigurationTest {

	@Test
	public void testConstructor() {
		SensorServiceConfiguration configuration = new SensorServiceConfiguration();
		assertEquals(0, configuration.getSendInterval());
	}

	@Test
	public void testGettersAndSetters() {
		SensorServiceConfiguration configuration = new SensorServiceConfiguration();
		configuration.setSendInterval(23);
		assertEquals(23, configuration.getSendInterval());
	}
}
