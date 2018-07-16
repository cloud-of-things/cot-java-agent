package com.telekom.cot.device.agent.sensor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.Instant;
import java.util.Date;

import org.junit.Test;

import com.telekom.cot.device.agent.platform.objects.SensorMeasurement;

public class SensorMeasurementTest {

	@Test
	public void testValueConstructor() {
		SensorMeasurement sensorMeasurement = new SensorMeasurement("c8y_Temperature", 23.4f, "�C");

		assertNotNull(sensorMeasurement.getTime());
		assertEquals("c8y_Temperature", sensorMeasurement.getType());
		assertEquals(23.4f, sensorMeasurement.getValue(), 0.00001f);
		assertEquals("�C", sensorMeasurement.getUnit());
	}
	
	@Test
	public void testSetters() {
		Date time = Date.from(Instant.parse("2018-01-01T12:00:00.00Z"));
		SensorMeasurement sensorMeasurement = new SensorMeasurement(null, -1.0f, null);
		sensorMeasurement.setTime(time);
		sensorMeasurement.setType("c8y_Temperature");
		sensorMeasurement.setValue(35.6f);
		sensorMeasurement.setUnit("�C");
		
		assertEquals(time, sensorMeasurement.getTime());
		assertEquals("c8y_Temperature", sensorMeasurement.getType());
		assertEquals(35.6f, sensorMeasurement.getValue(), 0.00001f);
		assertEquals("�C", sensorMeasurement.getUnit());
	}
}
