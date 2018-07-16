package com.telekom.cot.device.agent.raspbian.sensor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
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
import com.telekom.cot.device.agent.platform.objects.SensorMeasurement;
import com.telekom.cot.device.agent.sensor.SensorService;
import com.telekom.cot.device.agent.sensor.configuration.SensorConfiguration;

public class CpuTemperatureSensorTest {

	private static final String ALARM_TYPE = "TemperatureCriticalAlarm";
	private static final String ALARM_TEXT = "Critical alarm";
	private static final AlarmSeverity ALARM_SEVERITY = AlarmSeverity.CRITICAL;
	private static final float ALARM_MIN_VALUE = 25.0f;
	private static final float ALARM_MAX_VALUE = 40.0f;

	private CpuTemperatureSensorConfiguration configuration;

	@Mock
	private AlarmService mockAlarmService;
	@Mock
	private SensorService mockSensorService;
	@Mock
	private Runtime mockRuntime;
	@Mock
	private Process mockProcess;
	@Mock
	private InputStream mockInputStream;

	private CpuTemperatureSensor cpuTemperatureSensor = new CpuTemperatureSensor();

	@Before
	public void setUp() throws Exception {
		// initialize mocks
		MockitoAnnotations.initMocks(this);

		// initialize configuration
		ArrayList<SensorConfiguration.AlarmConfiguration> alarmConfigurations = new ArrayList<>();
		alarmConfigurations.add(new SensorConfiguration.AlarmConfiguration(ALARM_TYPE, ALARM_TEXT, ALARM_SEVERITY,
				ALARM_MIN_VALUE, ALARM_MAX_VALUE));
		configuration = new CpuTemperatureSensorConfiguration();
		configuration.setRecordReadingsInterval(1);
		configuration.setAlarmConfigurations(alarmConfigurations);

		// inject configuration, mocked services and mocked runtime
		// (overwritten by start())
		InjectionUtil.inject(cpuTemperatureSensor, configuration);
		InjectionUtil.inject(cpuTemperatureSensor, mockAlarmService);
        InjectionUtil.inject(cpuTemperatureSensor, mockSensorService);
		InjectionUtil.inject(cpuTemperatureSensor, mockRuntime);

		// mock runtime, process and input stream
		when(mockRuntime.exec(CpuTemperatureSensor.READ_CPU_TEMPERATURE_COMMAND)).thenReturn(mockProcess);
		when(mockProcess.getInputStream()).thenReturn(mockInputStream);
		when(mockInputStream.read()).thenReturn((int) '2', (int) '1', (int) '.', (int) '7', -1, (int) '2', (int) '3',
				(int) '.', (int) '4', -1, (int) '2', (int) '2', (int) '.', (int) '8', -1, (int) '2', (int) '4',
				(int) '.', (int) '9', -1, (int) '2', (int) '8', (int) '.', (int) '5', -1, (int) '2', (int) '0',
				(int) '.', (int) '3', -1);
	}

	/**
	 * test method start, no AlarmService given
	 */
	@Test(expected=SensorDeviceServiceException.class)
	public void testStartNoAlarmService() throws Exception {
        InjectionUtil.inject(cpuTemperatureSensor, "alarmService", null);
		cpuTemperatureSensor.start();
	}

    /**
     * test method start, no SensorService given
     */
    @Test(expected=SensorDeviceServiceException.class)
    public void testStartNoSensorService() throws Exception {
        InjectionUtil.inject(cpuTemperatureSensor, "sensorService", null);
        cpuTemperatureSensor.start();
    }

    /**
     * test method start
     */
    @Test
    public void testStart() throws Exception {
        cpuTemperatureSensor.start();
        cpuTemperatureSensor.stop();
    }

	/**
	 * test method stop
	 */
	@Test
	public void testStop() throws Exception {
		cpuTemperatureSensor.stop();
	}

	/**
	 * test method getTemperatureMeasurement, Runtime.exec throws Exception
	 */
	@Test(expected = SensorDeviceServiceException.class)
	public void testGetTemperatureMeasurementRuntimeExecException() throws Exception {
		// reset mocked runtime, throw exception on Runtime.exec call
		reset(mockRuntime);
		when(mockRuntime.exec(CpuTemperatureSensor.READ_CPU_TEMPERATURE_COMMAND)).thenThrow(new IOException());

		cpuTemperatureSensor.getTemperatureMeasurement();
	}

	/**
	 * test method getTemperatureMeasurement, Runtime.exec returns no process
	 * instance
	 */
	@Test(expected = SensorDeviceServiceException.class)
	public void testGetTemperatureMeasurementsNoProcess() throws Exception {
		// reset mocked runtime, return null on Runtime.exec call
		reset(mockRuntime);
		when(mockRuntime.exec(CpuTemperatureSensor.READ_CPU_TEMPERATURE_COMMAND)).thenReturn(null);

		cpuTemperatureSensor.getTemperatureMeasurement();
	}

	/**
	 * test method getTemperatureMeasurement, Process.getInputStream returns null
	 */
	@Test(expected = SensorDeviceServiceException.class)
	public void testGetTemperatureMeasurementsNoInputStream() throws Exception {
		// reset mocked process, return null on Process.getInputStream call
		reset(mockProcess);
		when(mockProcess.getInputStream()).thenReturn(null);

		cpuTemperatureSensor.getTemperatureMeasurement();
	}

	/**
	 * test method getTemperatureMeasurement, InputStream.read throws IOException
	 */
	@Test(expected = SensorDeviceServiceException.class)
	public void testGetTemperatureMeasurementsInputStreamException() throws Exception {
		// reset mocked input stream, throw IOException on InputStream.read call
		reset(mockInputStream);
		when(mockInputStream.read()).thenThrow(new IOException());

		cpuTemperatureSensor.getTemperatureMeasurement();
	}

	/**
	 * test method getTemperatureMeasurement, InputStream.read returns -1
	 */
	@Test(expected = SensorDeviceServiceException.class)
	public void testGetTemperatureMeasurementsInputStreamNoValues() throws Exception {
		// reset mocked input stream, InputStream.read returns -1
		reset(mockInputStream);
		when(mockInputStream.read()).thenReturn(-1);

		cpuTemperatureSensor.getTemperatureMeasurement();
	}

	/**
	 * test method getTemperatureMeasurement, InputStream.read returns not a number
	 * string
	 */
	@Test(expected = SensorDeviceServiceException.class)
	public void testGetTemperatureMeasurementsInputStreamNoNumber() throws Exception {
		// reset mocked input stream, InputStream.read returns not a number string
		reset(mockInputStream);
		when(mockInputStream.read()).thenReturn(((int) 'a'), -1);

		cpuTemperatureSensor.getTemperatureMeasurement();
	}

	/**
	 * test method getTemperatureMeasurement
	 */
	@Test
	public void testGetTemperatureMeasurements() throws Exception {
		SensorMeasurement measurement = cpuTemperatureSensor.getTemperatureMeasurement();

		assertNotNull(measurement);
		assertNotNull(measurement.getTime());
		assertEquals(21.7f, measurement.getValue(), 0.00001f);
		assertEquals("°C", measurement.getUnit());
	}

	/**
	 * test complete CpuTemperatureSensor
	 */
	@Test
	public void testComplete() throws Exception {
		cpuTemperatureSensor.start();
		InjectionUtil.inject(cpuTemperatureSensor, mockRuntime);
		TimeUnit.MILLISECONDS.sleep(5500);
		cpuTemperatureSensor.stop();

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
		assertEquals(20.3f, capturedMeasurements.get(5).getValue(), 0.0001f);

		// verify that an alarm has been created for the measurement with value=28.5
		verify(mockAlarmService, times(1)).createAlarm(ALARM_TYPE, ALARM_SEVERITY, ALARM_TEXT, null);
	}
}
