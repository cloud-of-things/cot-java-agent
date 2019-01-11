package com.telekom.cot.device.agent.measurement;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.MeasurementServiceException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.device.DeviceService;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.platform.objects.SensorMeasurement;
import com.telekom.cot.device.agent.service.AgentServiceProvider;
import com.telekom.cot.device.agent.service.channel.QueueChannel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MeasurementServiceImplTest {

	@Mock
	private Logger mockLogger;
	@Mock
	private AgentServiceProvider mockServiceProvider;
	@Mock
	private PlatformService mockPlatformService;
	@Mock
	private DeviceService mockDeviceService;
	@Mock
	QueueChannel<SensorMeasurement> queueChannel;

	private MeasurementServiceConfiguration configuration;

	private MeasurementServiceImpl measurementServiceImpl = new MeasurementServiceImpl();

	@Before
	public void setUp() throws Exception {
		// initialize mocks
		MockitoAnnotations.initMocks(this);

		// initialize configuration
		configuration = new MeasurementServiceConfiguration();
		configuration.setSendInterval(1);
        InjectionUtil.inject(measurementServiceImpl, configuration);

		// inject mocks "Logger", "AgentServiceProvider" and configuration
		InjectionUtil.injectStatic(MeasurementServiceImpl.class, mockLogger);
		InjectionUtil.inject(measurementServiceImpl, mockServiceProvider);
		InjectionUtil.inject(measurementServiceImpl, mockDeviceService);
        InjectionUtil.inject(measurementServiceImpl, mockPlatformService);
		InjectionUtil.inject(measurementServiceImpl, configuration);

		// mock platform service
		doNothing().when(mockPlatformService).createEvent(any(Date.class), any(String.class), any(String.class), any(String.class));
		when(mockDeviceService.getQueueChannel()).thenReturn(queueChannel);

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
		measurementServiceImpl.start();
		measurementServiceImpl.start();
	}

	/**
	 * test method start, AgentServiceProvider can't get a PlatformService instance
	 */
	@Test(expected = MeasurementServiceException.class)
	public void testStartNoPlatformService() throws Exception {
        InjectionUtil.inject(measurementServiceImpl, "platformService", null);
		measurementServiceImpl.start();
	}

	/**
	 * test method start
	 */
	@Test
	public void testStart() throws Exception {
		measurementServiceImpl.start();

		assertTrue(measurementServiceImpl.isStarted());
		verify(mockLogger).debug("start " + MeasurementServiceImpl.class.getSimpleName());
	}

	/**
	 * test method stop
	 */
	@Test
	public void testStop() throws Exception {
		measurementServiceImpl.start();
        assertTrue(measurementServiceImpl.isStarted());
		measurementServiceImpl.stop();
        assertFalse(measurementServiceImpl.isStarted());

		verify(mockLogger).debug("stop " + MeasurementServiceImpl.class.getSimpleName());
	}

	/**
	 * tests method stop when the sensor service has not been started
	 */
	@Test
	public void testStopNotStarted() throws Exception {
		measurementServiceImpl.stop();
        assertFalse(measurementServiceImpl.isStarted());
        verify(mockLogger, never()).debug("stop " + MeasurementServiceImpl.class.getSimpleName());
	}


	/**
	 * test methods sendMeasurements and getMeasurements
	 */
	@Test
	public void testGetAndSendMeasurements() throws Exception {
		SensorMeasurement sensorMeasurement = new SensorMeasurement("test", 9000f, "°C");
		List<SensorMeasurement> sensorMeasurements = new ArrayList<>();
		sensorMeasurements.add(sensorMeasurement);

		when(queueChannel.getItem()).thenReturn(sensorMeasurement)	//first call
									.thenReturn(null);				//second call


		measurementServiceImpl.start();
		//wait until measurements sent
		TimeUnit.MILLISECONDS.sleep(1100);

		verify(mockPlatformService, atLeastOnce()).createMeasurements(any());
	}

	/**
	 * test method sendMeasurements with PlatformService throwing Exceptioon
	 */
	@Test
	public void testSendMeasurementsFailure() throws Exception {
				SensorMeasurement sensorMeasurement = new SensorMeasurement("test", 9000f, "°C");
		List<SensorMeasurement> sensorMeasurements = new ArrayList<>();
		sensorMeasurements.add(sensorMeasurement);

		when(queueChannel.getItem()).thenReturn(sensorMeasurement)	//first call
				.thenReturn(null);				//second call
		Mockito.doThrow(new PlatformServiceException("test")).when(mockPlatformService).createMeasurements(sensorMeasurements);



		measurementServiceImpl.start();
		//wait until measurements sent
		TimeUnit.MILLISECONDS.sleep(1100);


		verify(mockLogger, atLeastOnce()).error(eq("Couldn't send measurements"), any(PlatformServiceException.class));
	}
}
