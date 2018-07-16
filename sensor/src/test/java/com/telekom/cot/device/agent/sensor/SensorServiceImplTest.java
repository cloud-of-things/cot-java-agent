package com.telekom.cot.device.agent.sensor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentServiceNotFoundException;
import com.telekom.cot.device.agent.common.exc.SensorDeviceServiceException;
import com.telekom.cot.device.agent.common.exc.SensorServiceException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.platform.objects.SensorMeasurement;
import com.telekom.cot.device.agent.sensor.configuration.SensorServiceConfiguration;
import com.telekom.cot.device.agent.service.AgentServiceProvider;

public class SensorServiceImplTest {

	@Mock
	private Logger mockLogger;
	@Mock
	private AgentServiceProvider mockServiceProvider;
	@Mock
	private PlatformService mockPlatformService;
	@Mock
	private SensorDeviceService mockSensor;

	private SensorServiceConfiguration configuration;
	private List<SensorDeviceService> sensorList;

	private SensorServiceImpl sensorServiceImpl = new SensorServiceImpl();

	@Before
	public void setUp() throws Exception {
		// initialize mocks
		MockitoAnnotations.initMocks(this);

		// initialize configuration
		configuration = new SensorServiceConfiguration();
		configuration.setSendInterval(1);
        InjectionUtil.inject(sensorServiceImpl, configuration);

		// inject mocks "Logger", "AgentServiceProvider" and configuration
		InjectionUtil.injectStatic(SensorServiceImpl.class, mockLogger);
		InjectionUtil.inject(sensorServiceImpl, mockServiceProvider);
        InjectionUtil.inject(sensorServiceImpl, mockPlatformService);
		InjectionUtil.inject(sensorServiceImpl, configuration);

		// add mocked SensorDeviceService to list
		sensorList = new ArrayList<SensorDeviceService>();
		sensorList.add(mockSensor);

		// return values of AgentServiceProvider
		when(mockServiceProvider.getServices(SensorDeviceService.class)).thenReturn(sensorList);

		// mock platform service
		doNothing().when(mockPlatformService).createEvent(any(Date.class), any(String.class), any(String.class), any(String.class));

		// mock sensor
		List<SensorMeasurement> measurementList = new ArrayList<SensorMeasurement>();
		measurementList.add(new SensorMeasurement("c8y_Temperature", 25.3f, "C"));
		measurementList.add(new SensorMeasurement("c8y_Temperature", 28.6f, "C"));
	}

	/**
	 * test method start, service has already been started
	 */
	@Test(expected = AbstractAgentException.class)
	public void testStartTwice() throws Exception {
		sensorServiceImpl.start();
		sensorServiceImpl.start();
	}

	/**
	 * test method start, AgentServiceProvider can't get a PlatformService instance
	 */
	@Test(expected = SensorServiceException.class)
	public void testStartNoPlatformService() throws Exception {
        InjectionUtil.inject(sensorServiceImpl, "platformService", null);
		sensorServiceImpl.start();
	}

	/**
	 * test method start, AgentServiceProvider can't get a SensorDeviceService
	 * instance
	 */
	@Test
	public void testStartNoSensorDeviceServices() throws Exception {
		AbstractAgentException e = new AgentServiceNotFoundException("service not found");
		doThrow(e).when(mockServiceProvider).getServices(SensorDeviceService.class);

		sensorServiceImpl.start();

		verify(mockLogger).warn("No sensor service found", e);
		verify(mockLogger, never()).info(eq("started sensor device service '{}'"), any(SensorDeviceService.class));
		verify(mockLogger, never()).error(eq("sensor service '{}' can't be started"), any(SensorDeviceService.class),
				any(AbstractAgentException.class));
	}

	/**
	 * test method start, SensorDeviceService.start throws an exception
	 */
	@Test
	public void testStartSensorDeviceServiceStartException() throws Exception {
		reset(mockSensor);
		SensorDeviceServiceException e = new SensorDeviceServiceException("Can't start service");
		doThrow(e).when(mockSensor).start();

		sensorServiceImpl.start();

		verify(mockLogger).error("sensor service '{}' can't be started", mockSensor, e);
	}

	/**
	 * test method start
	 */
	@Test
	public void testStart() throws Exception {
		sensorServiceImpl.start();

		verify(mockLogger).info("started sensor device service '{}'", mockSensor);
	}

	/**
	 * test method stop
	 */
	@Test
	public void testStop() throws Exception {
		sensorServiceImpl.start();
		sensorServiceImpl.stop();

		verify(mockSensor).stop();
	}

	/**
	 * tests method stop when the sensor service has not been started
	 */
	@Test
	public void testStopNotStarted() throws Exception {
		sensorServiceImpl.stop();
		verify(mockSensor, never()).stop();
		verify(mockLogger, never()).debug(any());
	}

	/**
	 * test method stop, SensorDeviceService.start throws an exception
	 */
	@Test
	public void testStopSensorDeviceServiceStartException() throws Exception {
		reset(mockSensor);
		doThrow(new SensorDeviceServiceException("Can't start service")).when(mockSensor).start();

		sensorServiceImpl.start();
		sensorServiceImpl.stop();
		verify(mockSensor, never()).stop();
	}
}
