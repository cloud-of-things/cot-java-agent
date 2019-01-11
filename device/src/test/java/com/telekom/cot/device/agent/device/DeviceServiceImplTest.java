package com.telekom.cot.device.agent.device;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentServiceNotFoundException;
import com.telekom.cot.device.agent.common.exc.DeviceServiceException;
import com.telekom.cot.device.agent.common.exc.SensorDeviceServiceException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.device.sensordevice.SensorDeviceService;
import com.telekom.cot.device.agent.device.sensordevice.TemperatureSensor;
import com.telekom.cot.device.agent.service.AgentServiceProvider;
import com.telekom.cot.device.agent.service.channel.Channel;



public class DeviceServiceImplTest {

	private static final Long handlersShutdownTimeout = 100L;
	
	private DeviceServiceImpl deviceService;
	private DeviceServiceConfiguration deviceServiceConf;
	
    @Mock
    private Logger mockLogger;
	
	@Mock
	private AgentServiceProvider mockServiceProvider;
	
	@Mock
	private TemperatureSensor mockTemperatureSensor;
	
	/**
	 * Initial setup for every unit test
	 * @throws AbstractAgentException
	 */
	@Before
	public void setUp() throws AbstractAgentException {
		deviceService = new DeviceServiceImpl();
		deviceServiceConf = new DeviceServiceConfiguration();
		deviceServiceConf.setHandlersShutdownTimeout(handlersShutdownTimeout);
		
        // initialize mocks
        MockitoAnnotations.initMocks(this);
        InjectionUtil.injectStatic(deviceService.getClass(), mockLogger);
        InjectionUtil.inject(deviceService, mockServiceProvider);
	}

	/**
	 * Start without setting any configuration
	 * Expecting failed start and a DeviceServiceException
	 * @throws AbstractAgentException
	 */
	@Test(expected=DeviceServiceException.class)
	public void testStartNoConfig() throws AbstractAgentException {
		deviceService.start();
	}
	
	/**
	 * Start with a valid SensorDeviceService implementation
	 * Should result in a successful start of DeviceService and used SensorDeviceServices
	 * @throws AbstractAgentException
	 */
	@Test
	public void testStartWithSensor() throws AbstractAgentException {
		InjectionUtil.inject(deviceService, deviceServiceConf);
		
        List<SensorDeviceService> handlers = new ArrayList<>();
        handlers.add(mockTemperatureSensor);

        when(mockServiceProvider.getServices(SensorDeviceService.class)).thenReturn(handlers);
		
		deviceService.start();
        
		verify(mockServiceProvider, times(1)).getServices(SensorDeviceService.class);
		verify(mockTemperatureSensor, times(1)).start();
	}
	
	/**
	 * Start with no valid SensorDeviceService implementation
	 * Should result in a successful start of DeviceService but no start of SensorDeviceServices
	 * @throws AbstractAgentException
	 */
	@Test
	public void testStartWithoutSensor() throws AbstractAgentException {
		AgentServiceNotFoundException exc = new AgentServiceNotFoundException("No sensor device service found");
		
		InjectionUtil.inject(deviceService, deviceServiceConf);

        when(mockServiceProvider.getServices(SensorDeviceService.class)).thenThrow(exc);
		
		deviceService.start();
        
		verify(mockServiceProvider, times(1)).getServices(SensorDeviceService.class);
		verify(mockTemperatureSensor, times(0)).start();
		verify(mockLogger).warn("No sensor device service found", exc);
	}
	
	/**
	 * Start with a invalid SensorDeviceService implementation
	 * Should result in a successful start of DeviceService but no start of SensorDeviceServices
	 * @throws AbstractAgentException
	 */
	@Test
	public void testStartWithBrokenSensor() throws AbstractAgentException {
		SensorDeviceServiceException exc = new SensorDeviceServiceException("test");
		
		InjectionUtil.inject(deviceService, deviceServiceConf);
		
        List<SensorDeviceService> handlers = new ArrayList<>();
        handlers.add(mockTemperatureSensor);

        when(mockServiceProvider.getServices(SensorDeviceService.class)).thenReturn(handlers);
        doThrow(exc).when(mockTemperatureSensor).start();
		
		deviceService.start();
        
		verify(mockServiceProvider, times(1)).getServices(SensorDeviceService.class);
		verify(mockLogger).error("sensor device service '{}' cannot be started", mockTemperatureSensor, exc);
	}
	
	/** 
	 * Start the DeviceService a second time after first successful start
	 * Should result in a DeviceServiceException
	 * @throws AbstractAgentException
	 */
	@Test(expected=DeviceServiceException.class)
	public void testAlreadyStarted() throws AbstractAgentException {
		InjectionUtil.inject(deviceService, deviceServiceConf);
		
        List<SensorDeviceService> handlers = new ArrayList<>();
        handlers.add(mockTemperatureSensor);

        when(mockServiceProvider.getServices(SensorDeviceService.class)).thenReturn(handlers);
		
        //start
		deviceService.start();
		
		//start another time
		deviceService.start();
	}
	
	/**
	 * Stop the DeviceService with valid sensors running
	 * Expecting a successful stop
	 * @throws AbstractAgentException
	 */
	@Test
	public void testStopWithSensor() throws AbstractAgentException {
		InjectionUtil.inject(deviceService, deviceServiceConf);
		
        List<SensorDeviceService> handlers = new ArrayList<>();
        handlers.add(mockTemperatureSensor);

        when(mockServiceProvider.getServices(SensorDeviceService.class)).thenReturn(handlers);
		
		deviceService.start();
		
		deviceService.stop();
		
		verify(mockLogger).debug("stop device service");
	}
	
	/**
	 * Stop the DeviceService with no valid sensors running
	 * Expecting a successful stop
	 * @throws AbstractAgentException
	 */
	@Test
	public void testStopWithoutSensor() throws AbstractAgentException {
		AgentServiceNotFoundException exc = new AgentServiceNotFoundException("No sensor device service found");
		
		InjectionUtil.inject(deviceService, deviceServiceConf);
		
		when(mockServiceProvider.getServices(SensorDeviceService.class)).thenThrow(exc);
		
		deviceService.start();
		
		deviceService.stop();
		
		verify(mockLogger).debug("stop device service");
	}
	
	/**
	 * Test Getter for the QueueChannel
	 * Should return the Channel instance of the DeviceService, implementing the Channel Interface
	 * @throws AbstractAgentException
	 */
	@Test
	public void testGetQueueChannel() throws AbstractAgentException {
		InjectionUtil.inject(deviceService, deviceServiceConf);
		
		List<SensorDeviceService> handlers = new ArrayList<>();
        handlers.add(mockTemperatureSensor);

        when(mockServiceProvider.getServices(SensorDeviceService.class)).thenReturn(handlers);
		
        deviceService.start();
        
        Assert.assertThat(deviceService.getQueueChannel(), instanceOf(Channel.class));;
	}
}