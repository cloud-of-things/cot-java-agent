package com.telekom.cot.device.agent.sensor.configuration;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import com.telekom.cot.device.agent.common.AlarmSeverity;

public class SensorConfigurationTest {

	private static final String ALARM_TYPE = "alarmType";
	private static final String ALARM_TEXT = "alarmText";
	private static final float ALARM_MIN_VALUE = -12.7f;
	private static final float ALARM_MAX_VALUE = 263.8f;
	private static final AlarmSeverity ALARM_SEVERITY = AlarmSeverity.WARNING;
	
	@Test
	public void testConstructor() {
		SensorConfiguration configuration = new SensorConfiguration() {};

		assertEquals(0, configuration.getRecordReadingsInterval());
		assertNull(configuration.getAlarmConfigurations());
	}

	@Test
	public void testGettersAndSetters() {
		ArrayList<SensorConfiguration.AlarmConfiguration> alarmConfigurations = new ArrayList<>();
		
		SensorConfiguration configuration = new SensorConfiguration() {};
		configuration.setRecordReadingsInterval(123);
		configuration.setAlarmConfigurations(alarmConfigurations);

		assertEquals(123, configuration.getRecordReadingsInterval());
		assertSame(alarmConfigurations, configuration.getAlarmConfigurations());
	}

	@Test
	public void testAlarmConfigurationConstructor() {
		SensorConfiguration.AlarmConfiguration configuration = new SensorConfiguration.AlarmConfiguration();
		
		assertNull(configuration.getType());
		assertNull(configuration.getText());
		assertEquals(Float.NEGATIVE_INFINITY, configuration.getMinValue(), 0.0f);
		assertEquals(Float.POSITIVE_INFINITY, configuration.getMaxValue(), 0.0f);
		assertEquals(AlarmSeverity.UNDEFINED, configuration.getSeverity());
	}

	@Test
	public void testAlarmConfigurationGettersAndSetters() {
		SensorConfiguration.AlarmConfiguration configuration = new SensorConfiguration.AlarmConfiguration();
		configuration.setType(ALARM_TYPE);
		configuration.setText(ALARM_TEXT);
		configuration.setMinValue(ALARM_MIN_VALUE);
		configuration.setMaxValue(ALARM_MAX_VALUE);
		configuration.setSeverity(ALARM_SEVERITY);
		
		assertEquals(ALARM_TYPE, configuration.getType());
		assertEquals(ALARM_TEXT, configuration.getText());
		assertEquals(ALARM_MIN_VALUE, configuration.getMinValue(), 0.0f);
		assertEquals(ALARM_MAX_VALUE, configuration.getMaxValue(), 0.0f);
		assertEquals(ALARM_SEVERITY, configuration.getSeverity());

		configuration.setSeverity("CRITICAL");
		assertEquals(AlarmSeverity.CRITICAL, configuration.getSeverity());
	}
}
