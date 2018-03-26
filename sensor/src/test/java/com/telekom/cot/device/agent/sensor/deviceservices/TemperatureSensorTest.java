package com.telekom.cot.device.agent.sensor.deviceservices;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.AlarmSeverity;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentServiceNotFoundException;
import com.telekom.cot.device.agent.common.exc.ConfigurationNotFoundException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.common.exc.SensorDeviceServiceException;
import com.telekom.cot.device.agent.common.util.InjectionUtil;
import com.telekom.cot.device.agent.sensor.AlarmService;
import com.telekom.cot.device.agent.sensor.SensorMeasurement;
import com.telekom.cot.device.agent.sensor.SensorService;
import com.telekom.cot.device.agent.sensor.configuration.SensorConfiguration;
import com.telekom.cot.device.agent.service.AgentServiceProvider;

public class TemperatureSensorTest {

	private static final String ALARM_TEXT = "Critical alarm";
	private static final String ALARM_TYPE = "TemperatureCriticalAlarm";
	private static final AlarmSeverity ALARM_SEVERITY = AlarmSeverity.CRITICAL;

	private SensorConfiguration configuration;
	private List<SensorMeasurement> sensorMeasurements;

	@Mock
	private AgentServiceProvider mockServiceProvider;
	@Mock
	private AlarmService mockSensorAlarmService;
	@Mock
	private SensorService mockSensorService;
	@Mock
	private Logger mockLogger;

	TemperatureSensor temperatureSensor = new TemperatureSensorDeviceServiceImpl();

	@Before
	public void setUp() throws Exception {
		// initialize mocks
		MockitoAnnotations.initMocks(this);

		// initialize configuration
		ArrayList<SensorConfiguration.AlarmConfiguration> alarmConfigurations = new ArrayList<>();
		alarmConfigurations
				.add(new SensorConfiguration.AlarmConfiguration(ALARM_TYPE, ALARM_TEXT, ALARM_SEVERITY, 25.0f, 40.0f));
		configuration = new SensorConfiguration() {
		};
		configuration.setRecordReadingsInterval(1);
		configuration.setAlarmConfigurations(alarmConfigurations);

		// initialize measurements
		sensorMeasurements = new ArrayList<>();
		sensorMeasurements.add(new SensorMeasurement("c8y_Temperature", 21.7f, "°C"));
		sensorMeasurements.add(new SensorMeasurement("c8y_Temperature", 23.4f, "°C"));
		sensorMeasurements.add(new SensorMeasurement("c8y_Temperature", 22.8f, "°C"));
		sensorMeasurements.add(new SensorMeasurement("c8y_Temperature", 24.9f, "°C"));
		sensorMeasurements.add(new SensorMeasurement("c8y_Temperature", 28.5f, "°C"));

		// inject configuration, mocked AgentServiceProvider
		InjectionUtil.injectStatic(TemperatureSensor.class, mockLogger);
		InjectionUtil.inject(temperatureSensor, configuration);
		InjectionUtil.inject(temperatureSensor, mockServiceProvider);

		// return values of AgentServiceProvider
		when(mockServiceProvider.getService(AlarmService.class)).thenReturn(mockSensorAlarmService);
		when(mockServiceProvider.getService(SensorService.class)).thenReturn(mockSensorService);
	}

	/**
	 * test method start, no configuration is given
	 */
	@Test(expected = ConfigurationNotFoundException.class)
	public void testStartNoConfiguration() throws Exception {
		TemperatureSensor noConfigTemperatureSensor = new TemperatureSensor() {
			@Override
			protected SensorMeasurement getTemperatureMeasurement() {
				return null;
			}

			@Override
			protected SensorConfiguration getSensorConfiguration() throws AbstractAgentException {
				throw new ConfigurationNotFoundException("temperature sensor configuration not found");
			}
		};
		noConfigTemperatureSensor.start();
	}

	/**
	 * test method start, getService(AlarmService.class) throws an exception
	 */
	@Test(expected = AgentServiceNotFoundException.class)
	public void testStartNoAlarmService() throws Exception {
		doThrow(new AgentServiceNotFoundException("service not found")).when(mockServiceProvider)
				.getService(AlarmService.class);

		temperatureSensor.start();
	}

	/**
	 * test method start, getService(SensorService.class) throws an exception
	 */
	@Test(expected = AgentServiceNotFoundException.class)
	public void testStartNoSensorService() throws Exception {
		doThrow(new AgentServiceNotFoundException("service not found")).when(mockServiceProvider)
				.getService(SensorService.class);

		temperatureSensor.start();
	}

	/**
	 * test method getAlarmConfigurations, no alarm configurations (null)
	 */
	@Test
	public void testGetAlarmConfigurationsAlarmConfigurationsNull() throws Exception {
		configuration.setAlarmConfigurations(null);
		temperatureSensor.start();
	}

	/**
	 * test method recordReading, getTemperatureMeasurement throws exception so
	 * addMeasurement and createAlarm are never called
	 */
	@Test
	public void testRecordReadingGetTemperatureMeasurementException() throws Exception {
		TemperatureSensor exceptionTemperatureSensor = new TemperatureSensor() {
			@Override
			protected SensorMeasurement getTemperatureMeasurement() throws AbstractAgentException {
				throw new SensorDeviceServiceException("test");
			}

			@Override
			protected SensorConfiguration getSensorConfiguration() throws AbstractAgentException {
				return configuration;
			};
		};

		// inject configuration, mocked AgentServiceProvider
		InjectionUtil.inject(exceptionTemperatureSensor, configuration);
		InjectionUtil.inject(exceptionTemperatureSensor, mockServiceProvider);

		exceptionTemperatureSensor.start();
		Thread.sleep(500);
		exceptionTemperatureSensor.stop();

		verify(mockSensorService, never()).addMeasurement(any(SensorMeasurement.class));
		verify(mockSensorAlarmService, never()).createAlarm(any(), any(), any(), any(), any(), any());
	}

	/**
	 * test method recordReading, getTemperatureMeasurement returns null so
	 * addMeasurement and createAlarm are never called
	 */
	@Test
	public void testRecordReadingNullMeasurement() throws Exception {
		sensorMeasurements.clear();

		temperatureSensor.start();
		Thread.sleep(500);
		temperatureSensor.stop();

		verify(mockSensorService, never()).addMeasurement(any(SensorMeasurement.class));
		verify(mockSensorAlarmService, never()).createAlarm(any(), any(), any(), any(), any(), any());
	}

	/**
	 * test method checkAlarms, SensorAlarmService.sendAlarm throws exception error
	 * is logged, no exception thrown
	 */
	@Test
	public void testCheckAlarmsSendAlarmsException() throws Exception {
		reset(mockSensorAlarmService);
		PlatformServiceException e = new PlatformServiceException("Can't send alarm");
		doThrow(e).when(mockSensorAlarmService).createAlarm(ALARM_TYPE, ALARM_SEVERITY, ALARM_TEXT, null, null, null);

		sensorMeasurements.clear();
		sensorMeasurements.add(new SensorMeasurement("c8y_Temperature", 30.0f, "°C"));

		temperatureSensor.start();
		Thread.sleep(500);
		temperatureSensor.stop();

		verify(mockLogger).error("Can't send alarm '{}'({}, {})", ALARM_TEXT, ALARM_TYPE, ALARM_SEVERITY.getValue(), e);
	}

	/**
	 * test method checkAlarms, read temperature value is greater than alarm
	 * minValue and maxValue, so no alarm is created
	 */
	@Test
	public void testCheckAlarmsTemperatureGreaterMaxValue() throws Exception {
		sensorMeasurements.clear();
		sensorMeasurements.add(new SensorMeasurement("c8y_Temperature", 60.0f, "°C"));

		temperatureSensor.start();
		Thread.sleep(500);
		temperatureSensor.stop();

		verify(mockSensorAlarmService, never()).createAlarm(any(), any(), any(), any(), any(), any());
	}

	/**
	 * test method stop
	 */
	@Test
	public void testStop() throws Exception {
		temperatureSensor.stop();
	}

	/**
	 * test complete TemperatureSensor
	 */
	@Test
	public void testComplete() throws Exception {
		temperatureSensor.start();
		Thread.sleep(5500);
		temperatureSensor.stop();

		// assert that 6 measurements have been pushed to SensorService
		ArgumentCaptor<SensorMeasurement> measurementCaptor = ArgumentCaptor.forClass(SensorMeasurement.class);
		verify(mockSensorService, times(6)).addMeasurement(measurementCaptor.capture());

		// verify the measurement values that have been pushed to SensorService
		List<SensorMeasurement> capturedMeasurements = measurementCaptor.getAllValues();
		assertEquals(21.7f, capturedMeasurements.get(0).getValue(), 0.0001f);
		assertEquals(23.4f, capturedMeasurements.get(1).getValue(), 0.0001f);
		assertEquals(22.8f, capturedMeasurements.get(2).getValue(), 0.0001f);
		assertEquals(24.9f, capturedMeasurements.get(3).getValue(), 0.0001f);
		assertEquals(28.5f, capturedMeasurements.get(4).getValue(), 0.0001f);
		assertEquals(21.7f, capturedMeasurements.get(5).getValue(), 0.0001f);

		// verify that an alarm has been created for the measurement with value=28.5
		verify(mockSensorAlarmService, times(1)).createAlarm(ALARM_TYPE, ALARM_SEVERITY, ALARM_TEXT, null, null, null);
	}

	private class TemperatureSensorDeviceServiceImpl extends TemperatureSensor {
		private int measurementIndex = 0;

		@Override
		protected SensorMeasurement getTemperatureMeasurement() throws AbstractAgentException {
			if (sensorMeasurements.isEmpty()) {
				return null;
			}

			SensorMeasurement measurement = sensorMeasurements.get(measurementIndex);
			measurementIndex = (measurementIndex + 1) % sensorMeasurements.size();
			return measurement;
		}

		@Override
		protected SensorConfiguration getSensorConfiguration() throws AbstractAgentException {
			return configuration;
		}
	};
}
