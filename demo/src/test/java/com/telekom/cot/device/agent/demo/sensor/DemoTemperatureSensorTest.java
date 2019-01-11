package com.telekom.cot.device.agent.demo.sensor;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.telekom.cot.device.agent.alarm.AlarmService;
import com.telekom.cot.device.agent.common.AlarmSeverity;
import com.telekom.cot.device.agent.common.exc.SensorDeviceServiceException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.device.DeviceService;
import com.telekom.cot.device.agent.device.sensordevice.SensorConfiguration;
import com.telekom.cot.device.agent.platform.objects.SensorMeasurement;
import com.telekom.cot.device.agent.service.channel.QueueChannel;

public class DemoTemperatureSensorTest {

	private static final String TEMPERATURE_VALUES_FILE = "src/test/resources/temperatures.txt";
	private static final String ALARM_TYPE = "TemperatureCriticalAlarm";
	private static final AlarmSeverity ALARM_SEVERITY = AlarmSeverity.CRITICAL;
	private static final float ALARM_MIN_VALUE = 25.0f;
	private static final float ALARM_MAX_VALUE = 40.0f;
	private static final String ALARM_TEXT = "Critical alarm";

	private DemoTemperatureSensorConfiguration configuration;

	@Mock
	private AlarmService mockAlarmService;
	@Mock
	private DeviceService mockDeviceService;
	@Mock
	private QueueChannel<SensorMeasurement> mockQueueChannel;
	@Mock
	private TemperatureFileReader mockTemperatureFileReader;

	private DemoTemperatureSensor demoTemperatureSensor = new DemoTemperatureSensor();

	@Before
	public void setUp() throws Exception {
		// initialize mocks
		MockitoAnnotations.initMocks(this);

		// initialize configuration
		ArrayList<SensorConfiguration.AlarmConfiguration> alarmConfigurations = new ArrayList<>();
		alarmConfigurations.add(new SensorConfiguration.AlarmConfiguration(ALARM_TYPE, ALARM_TEXT, ALARM_SEVERITY,
				ALARM_MIN_VALUE, ALARM_MAX_VALUE));
		configuration = new DemoTemperatureSensorConfiguration();
		configuration.setRecordReadingsInterval(1);
		configuration.setAlarmConfigurations(alarmConfigurations);
		configuration.setValueFilePath(TEMPERATURE_VALUES_FILE);

		// inject configuration, mocked services and mocked
		// TemperatureFileReader
		InjectionUtil.inject(demoTemperatureSensor, configuration);
		InjectionUtil.inject(demoTemperatureSensor, mockAlarmService);
        InjectionUtil.inject(demoTemperatureSensor, mockDeviceService);
		InjectionUtil.inject(demoTemperatureSensor, mockTemperatureFileReader);

		// mock TemperatureFileReader
		when(mockTemperatureFileReader.readFile(TEMPERATURE_VALUES_FILE)).thenReturn(true);
		when(mockTemperatureFileReader.getTemperatureMeasurement()).thenReturn(new Float(21.7f), new Float(23.4f),
				new Float(22.8f), new Float(24.9f), new Float(28.5f), new Float(21.7f), new Float(23.4f));
		
		// mock mockDeviceService
		when(mockDeviceService.getQueueChannel()).thenReturn(mockQueueChannel);
	}

	/**
	 * test method start, configuration has no property "valueFilePath"
	 */
	@Test(expected = SensorDeviceServiceException.class)
	public void testStartConfigurationWithoutPathProperty() throws Exception {
		configuration.setValueFilePath(null);
		demoTemperatureSensor.start();
	}

	/**
	 * test method start, TemperatureFileReader.readFile returns false
	 */
	@Test(expected = SensorDeviceServiceException.class)
	public void testStartCantReadValueFile() throws Exception {
		when(mockTemperatureFileReader.readFile(TEMPERATURE_VALUES_FILE)).thenReturn(false);

		demoTemperatureSensor.start();
	}

	/**
	 * test method recordReading (run method for the worker thread),
	 * TemperatureFileReader.getTemperatureMeasurement returns null
	 * so getQueueChannel().add() and createAlarm() are never called 
	 */
	@Test
	public void testStartNullMeasurement() throws Exception {
		reset(mockTemperatureFileReader);
		when(mockTemperatureFileReader.readFile(TEMPERATURE_VALUES_FILE)).thenReturn(true);
		when(mockTemperatureFileReader.getTemperatureMeasurement()).thenReturn(null);

		demoTemperatureSensor.start();
		TimeUnit.MILLISECONDS.sleep(500);
		demoTemperatureSensor.stop();

		verify(mockDeviceService, never()).getQueueChannel();
		verify(mockAlarmService, never()).createAlarm(any(), any(), any(), any());
	}

	/**
	 * test complete DemoTemperatureSensor
	 */
	@Test
	public void testComplete() throws Exception {
		demoTemperatureSensor.start();
        TimeUnit.MILLISECONDS.sleep(5500);
		demoTemperatureSensor.stop();

		// assert that 6 measurements have been pushed to MeasurementService
		ArgumentCaptor<SensorMeasurement> measurementCaptor = ArgumentCaptor.forClass(SensorMeasurement.class);
		verify(mockQueueChannel, times(6)).add(measurementCaptor.capture());

		// verify the measurement values that have been pushed to MeasurementService
		List<SensorMeasurement> capturedMeasurements = measurementCaptor.getAllValues();
		assertEquals(21.7f, capturedMeasurements.get(0).getValue(), 0.0001f);
		assertEquals(23.4f, capturedMeasurements.get(1).getValue(), 0.0001f);
		assertEquals(22.8f, capturedMeasurements.get(2).getValue(), 0.0001f);
		assertEquals(24.9f, capturedMeasurements.get(3).getValue(), 0.0001f);
		assertEquals(28.5f, capturedMeasurements.get(4).getValue(), 0.0001f);
		assertEquals(21.7f, capturedMeasurements.get(5).getValue(), 0.0001f);

		// verify that an alarm has been created for the measurement with value=28.5
		verify(mockAlarmService, times(1)).createAlarm(ALARM_TYPE, ALARM_SEVERITY, ALARM_TEXT, null);
	}
}
