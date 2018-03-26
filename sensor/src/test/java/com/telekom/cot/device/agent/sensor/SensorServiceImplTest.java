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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
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
import com.telekom.cot.device.agent.inventory.InventoryService;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.sensor.configuration.SensorServiceConfiguration;
import com.telekom.cot.device.agent.service.AgentServiceProvider;
import com.telekom.cot.device.agent.service.configuration.ConfigurationManager;
import com.telekom.m2m.cot.restsdk.identity.ExternalId;
import com.telekom.m2m.cot.restsdk.inventory.ManagedObject;

public class SensorServiceImplTest {

	private static final String EVENT_TYPE = "eventType";
	private static final String EVENT_TEXT = "eventText";
	private static final HashMap<String, Object> EVENT_ATTRIBUTES = new HashMap<>();
	private static final String EVENT_OBJECT = "eventObject";

	private static final String ALARM_TYPE = "alarmType";
	private static final AlarmSeverity ALARM_SEVERITY = AlarmSeverity.CRITICAL;
	private static final String ALARM_TEXT = "alarmText";
	private static final String ALARM_STATUS = "alarmStatus";
	private static final HashMap<String, Object> ALARM_ATTRIBUTES = new HashMap<>();
	private static final String ALARM_OBJECT = "alarmObject";

	@Mock
	private Logger mockLogger;
	@Mock
	private AgentServiceProvider mockServiceProvider;
	@Mock
	private ConfigurationManager mockConfigurationManager;
	@Mock
	private PlatformService mockPlatformService;
	@Mock
	private InventoryService mockInventoryService;
	@Mock
	private SensorDeviceService mockSensor;
	@Mock
	private ExternalId mockExternalId;
	@Mock
	private ManagedObject mockManagedObject;

	private SensorServiceConfiguration configuration;
	private List<SensorDeviceService> sensorList;

	private SensorServiceImpl sensorServiceImpl = new SensorServiceImpl();

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		// initialize mocks
		MockitoAnnotations.initMocks(this);

		// initialize configuration
		configuration = new SensorServiceConfiguration();
		configuration.setSendInterval(1);

		// inject mocks "Logger", "AgentServiceProvider" and configuration
		InjectionUtil.injectStatic(SensorServiceImpl.class, mockLogger);
		InjectionUtil.inject(sensorServiceImpl, mockServiceProvider);
		InjectionUtil.inject(sensorServiceImpl, mockConfigurationManager);
		InjectionUtil.inject(sensorServiceImpl, configuration);

		// add mocked SensorDeviceService to list
		sensorList = new ArrayList<SensorDeviceService>();
		sensorList.add(mockSensor);

		// return values of AgentServiceProvider
		when(mockServiceProvider.getService(PlatformService.class)).thenReturn(mockPlatformService);
		when(mockServiceProvider.getServices(SensorDeviceService.class)).thenReturn(sensorList);

		// behavior of mocked ConfigurationManager
		when(mockConfigurationManager.getConfiguration(SensorServiceConfiguration.class)).thenReturn(configuration);

		// mock platform service
		when(mockPlatformService.getExternalId()).thenReturn(mockExternalId);
		when(mockPlatformService.getManagedObject()).thenReturn(mockManagedObject);
		when(mockPlatformService.createMeasurements(any())).then(AdditionalAnswers.returnsFirstArg());
		doNothing().when(mockPlatformService).createEvent(any(Date.class), any(String.class), any(String.class),
				any(Map.class), any());

		// mock sensor
		List<SensorMeasurement> measurementList = new ArrayList<SensorMeasurement>();
		measurementList.add(new SensorMeasurement("c8y_Temperature", 25.3f, "C"));
		measurementList.add(new SensorMeasurement("c8y_Temperature", 28.6f, "C"));
		// when(mockSensor.getMeasurements()).thenReturn(measurementList);
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
	@Test(expected = AbstractAgentException.class)
	public void testStartNoPlatformService() throws Exception {
		AgentServiceNotFoundException e = new AgentServiceNotFoundException("service not found");
		doThrow(e).when(mockServiceProvider).getService(PlatformService.class);

		sensorServiceImpl.start();
	}

	/**
	 * test method start, no configuration is given
	 */
	@Test(expected = ConfigurationNotFoundException.class)
	public void testStartNoConfiguration() throws Exception {
		ConfigurationNotFoundException e = new ConfigurationNotFoundException("configuration not found");
		doThrow(e).when(mockConfigurationManager).getConfiguration(SensorServiceConfiguration.class);

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

	/**
	 * test method createEvent, PlatformService.createEvent() throws exception
	 */
	@Test(expected = PlatformServiceException.class)
	public void testCreateEventCreateEventException() throws Exception {
		// throw exception when calling "PlatformService.createEvent"
		doThrow(new PlatformServiceException("can't create event")).when(mockPlatformService)
				.createEvent(any(Date.class), eq(EVENT_TYPE), eq(EVENT_TEXT), eq(EVENT_ATTRIBUTES), eq(EVENT_OBJECT));
		InjectionUtil.inject(sensorServiceImpl, mockPlatformService);

		sensorServiceImpl.createEvent(EVENT_TYPE, EVENT_TEXT, EVENT_ATTRIBUTES, EVENT_OBJECT);
	}

	/**
	 * test method createEvent
	 */
	@Test
	public void testCreateEvent() throws Exception {
		InjectionUtil.inject(sensorServiceImpl, mockPlatformService);

		sensorServiceImpl.createEvent(EVENT_TYPE, EVENT_TEXT, EVENT_ATTRIBUTES, EVENT_OBJECT);

		verify(mockLogger).debug("create and send event of type '{}' with text '{}'", EVENT_TYPE, EVENT_TEXT);
		verify(mockPlatformService).createEvent(any(Date.class), eq(EVENT_TYPE), eq(EVENT_TEXT), eq(EVENT_ATTRIBUTES),
				eq(EVENT_OBJECT));
	}

	/**
	 * test method createAlarm, PlatformService.createAlarm() throws exception
	 */
	@Test(expected = PlatformServiceException.class)
	public void testCreateAlarmCreateAlarmException() throws Exception {
		// throw exception when calling "PlatformService.createAlarm"
		doThrow(new PlatformServiceException("can't create alarm")).when(mockPlatformService).createAlarm(
				any(Date.class), eq(ALARM_TYPE), eq(ALARM_SEVERITY), eq(ALARM_TEXT), eq(ALARM_STATUS),
				eq(ALARM_ATTRIBUTES), eq(ALARM_OBJECT));
		InjectionUtil.inject(sensorServiceImpl, mockPlatformService);

		sensorServiceImpl.createAlarm(ALARM_TYPE, ALARM_SEVERITY, ALARM_TEXT, ALARM_STATUS, ALARM_ATTRIBUTES,
				ALARM_OBJECT);
	}

	/**
	 * test method createAlarm
	 */
	@Test
	public void testCreateAlarm() throws Exception {
		InjectionUtil.inject(sensorServiceImpl, mockPlatformService);

		sensorServiceImpl.createAlarm(ALARM_TYPE, ALARM_SEVERITY, ALARM_TEXT, ALARM_STATUS, ALARM_ATTRIBUTES,
				ALARM_OBJECT);

		verify(mockLogger).debug("create and send alarm of type {} with text {} and severity {}", ALARM_TYPE,
				ALARM_TEXT, ALARM_SEVERITY.getValue());
		verify(mockPlatformService).createAlarm(any(Date.class), eq(ALARM_TYPE), eq(ALARM_SEVERITY), eq(ALARM_TEXT),
				eq(ALARM_STATUS), eq(ALARM_ATTRIBUTES), eq(ALARM_OBJECT));
	}
}
