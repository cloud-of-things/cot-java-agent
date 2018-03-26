package com.telekom.cot.device.agent.app;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.util.InjectionUtil;
import com.telekom.cot.device.agent.credentials.DeviceCredentialsService;
import com.telekom.cot.device.agent.inventory.InventoryService;
import com.telekom.cot.device.agent.operation.OperationService;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.sensor.EventService;
import com.telekom.cot.device.agent.sensor.SensorService;
import com.telekom.cot.device.agent.service.AgentServiceManager;
import com.telekom.cot.device.agent.service.configuration.AgentCredentials;
import com.telekom.cot.device.agent.service.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.service.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.system.SystemService;

public class AppBootstrapTest {

	@Mock
	private Logger mockLogger;
	@Mock
	private ConfigurationManager mockConfigurationManager;
	@Mock
	private AgentCredentialsManager mockAgentCredentialsManager;
	@Mock
	private AgentServiceManager mockAgentServiceManager;
	@Mock
	private SystemService mockSystemService;
	@Mock
	private PlatformService mockPlatformService;
	@Mock
	private InventoryService mockInventoryService;
	@Mock
	private OperationService mockOperationService;
	@Mock
	private SensorService mockSensorService;
	@Mock
	private EventService mockEventService;
	@Mock
	private DeviceCredentialsService mockDeviceCredentialsService;

	private AppBootstrap bootstrap = AppBootstrap.getInstance("configurationFile", "deviceCredentialsFile");

	@Before
	public void setUp() throws Exception {
		// init and inject mocks
		MockitoAnnotations.initMocks(this);
		InjectionUtil.injectStatic(AppBootstrap.class, mockLogger);
		InjectionUtil.inject(bootstrap, mockConfigurationManager);
		InjectionUtil.inject(bootstrap, mockAgentCredentialsManager);
		InjectionUtil.inject(bootstrap, mockAgentServiceManager);

		// behavior of mocked AgentServiceManager
		when(mockAgentServiceManager.getService(SystemService.class)).thenReturn(mockSystemService);
		when(mockAgentServiceManager.getService(PlatformService.class)).thenReturn(mockPlatformService);
		when(mockAgentServiceManager.getService(InventoryService.class)).thenReturn(mockInventoryService);
		when(mockAgentServiceManager.getService(OperationService.class)).thenReturn(mockOperationService);
		when(mockAgentServiceManager.getService(SensorService.class)).thenReturn(mockSensorService);
		when(mockAgentServiceManager.getService(EventService.class)).thenReturn(mockEventService);
		when(mockAgentServiceManager.getService(DeviceCredentialsService.class))
				.thenReturn(mockDeviceCredentialsService);
	}

	// Credentials are NOT available, device is NOT registered
	@Test
	public void testStartCredentialsNotAvailableDeviceNotRegistered() throws AppMainException, AbstractAgentException {
		when(mockDeviceCredentialsService.credentialsAvailable()).thenReturn(false);
		when(mockDeviceCredentialsService.requestCredentials())
				.thenReturn(new AgentCredentials("tenant", "username", "password"));
		when(mockInventoryService.isDeviceRegistered()).thenReturn(false);

		// test
		bootstrap.start();

		Mockito.verify(mockAgentServiceManager).loadAndInitServices(mockConfigurationManager,
				mockAgentCredentialsManager);
		Mockito.verify(mockSystemService).start();
		Mockito.verify(mockDeviceCredentialsService).start();
		Mockito.verify(mockAgentCredentialsManager).writeCredentials(any());
		Mockito.verify(mockDeviceCredentialsService).stop();
		Mockito.verify(mockPlatformService).start();
		Mockito.verify(mockInventoryService).start();
		Mockito.verify(mockInventoryService, never()).updateDevice();
		Mockito.verify(mockInventoryService).createAndRegisterDevice();
		Mockito.verify(mockOperationService).start();
		Mockito.verify(mockSensorService).start();
		Mockito.verify(mockEventService).createEvent(any(), any(), any(), any());
	}

	// Credentials are NOT available, device is registered
	@Test
	public void testStartCredentialsNotAvailableDeviceRegistered() throws AppMainException, AbstractAgentException {
		when(mockDeviceCredentialsService.credentialsAvailable()).thenReturn(false);
		when(mockDeviceCredentialsService.requestCredentials())
				.thenReturn(new AgentCredentials("tenant", "username", "password"));
		when(mockInventoryService.isDeviceRegistered()).thenReturn(true);

		// test
		bootstrap.start();

		Mockito.verify(mockAgentServiceManager).loadAndInitServices(mockConfigurationManager,
				mockAgentCredentialsManager);
		Mockito.verify(mockSystemService).start();
		Mockito.verify(mockDeviceCredentialsService).start();
		Mockito.verify(mockAgentCredentialsManager).writeCredentials(any());
		Mockito.verify(mockDeviceCredentialsService).stop();
		Mockito.verify(mockPlatformService).start();
		Mockito.verify(mockInventoryService).start();
		Mockito.verify(mockInventoryService, never()).createAndRegisterDevice();
		Mockito.verify(mockInventoryService).updateDevice();
		Mockito.verify(mockOperationService).start();
		Mockito.verify(mockSensorService).start();
		Mockito.verify(mockEventService).createEvent(any(), any(), any(), any());
	}

	// Credentials are available, device is NOT registered
	@Test
	public void testStartCredentialsAvailableDeviceNotRegistered() throws AppMainException, AbstractAgentException {
		when(mockDeviceCredentialsService.credentialsAvailable()).thenReturn(true);
		when(mockInventoryService.isDeviceRegistered()).thenReturn(false);

		// test
		bootstrap.start();

		Mockito.verify(mockAgentServiceManager).loadAndInitServices(mockConfigurationManager,
				mockAgentCredentialsManager);
		Mockito.verify(mockSystemService).start();
		Mockito.verify(mockDeviceCredentialsService).start();
		Mockito.verify(mockDeviceCredentialsService, never()).requestCredentials();
		Mockito.verify(mockAgentCredentialsManager, never()).writeCredentials(any());
		Mockito.verify(mockDeviceCredentialsService).stop();
		Mockito.verify(mockPlatformService).start();
		Mockito.verify(mockInventoryService).start();
		Mockito.verify(mockInventoryService).createAndRegisterDevice();
		Mockito.verify(mockInventoryService, never()).updateDevice();
		Mockito.verify(mockOperationService).start();
		Mockito.verify(mockSensorService).start();
		Mockito.verify(mockEventService).createEvent(any(), any(), any(), any());
	}

	// Credentials are available, device is registered
	@Test
	public void testStartCredentialsAvailableDeviceRegistered() throws AppMainException, AbstractAgentException {
		when(mockDeviceCredentialsService.credentialsAvailable()).thenReturn(true);
		when(mockInventoryService.isDeviceRegistered()).thenReturn(true);

		// test
		bootstrap.start();

		Mockito.verify(mockAgentServiceManager).loadAndInitServices(mockConfigurationManager,
				mockAgentCredentialsManager);
		Mockito.verify(mockSystemService).start();
		Mockito.verify(mockDeviceCredentialsService).start();
		Mockito.verify(mockDeviceCredentialsService, never()).requestCredentials();
		Mockito.verify(mockAgentCredentialsManager, never()).writeCredentials(any());
		Mockito.verify(mockDeviceCredentialsService).stop();
		Mockito.verify(mockPlatformService).start();
		Mockito.verify(mockInventoryService).start();
		Mockito.verify(mockInventoryService, never()).createAndRegisterDevice();
		Mockito.verify(mockInventoryService).updateDevice();
		Mockito.verify(mockOperationService).start();
		Mockito.verify(mockSensorService).start();
		Mockito.verify(mockEventService).createEvent(any(), any(), any(), any());
	}
}
