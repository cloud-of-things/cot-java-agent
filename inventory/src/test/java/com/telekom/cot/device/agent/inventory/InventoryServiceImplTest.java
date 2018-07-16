package com.telekom.cot.device.agent.inventory;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.InventoryServiceException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.common.exc.PropertyNotFoundException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.platform.objects.AgentManagedObject;
import com.telekom.cot.device.agent.service.event.AgentContext;
import com.telekom.cot.device.agent.system.SystemService;
import com.telekom.cot.device.agent.system.properties.Properties;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AgentManagedObjectFactory.class)
public class InventoryServiceImplTest {

	private static final String DEVICE_TYPE = "testDeviceType";
	private static final String DEVICE_NAME = "testDevice";

	@Mock
	private PlatformService mockPlatformService;
	@Mock
	private AgentManagedObjectFactory mockManagedObjectFactory;
	@Mock
	private AgentContext mockAgentContext;

	private AgentManagedObject agentManagedObject;
	private InventoryServiceConfiguration configuration;
	private SystemService systemService;

	private InventoryServiceImpl inventoryServiceImpl = new InventoryServiceImpl();

	@Rule
	public ExpectedException exceptions = ExpectedException.none();

	@Before
	public void setUp() throws Exception {

		systemService = new SystemService() {
			@Override
			public void stop() throws AbstractAgentException {
			}

			@Override
			public void start() throws AbstractAgentException {
			}

			@Override
			public boolean isStarted() {
				return false;
			}

			@Override
			public <T extends Properties> T getProperties(Class<T> clazz) throws PropertyNotFoundException {
				throw new PropertyNotFoundException("");
			}

            @Override
            public void init(AgentContext agentContext) throws AbstractAgentException {
            }
		};

		// initialize mocks
		MockitoAnnotations.initMocks(this);
		PowerMockito.mockStatic(AgentManagedObjectFactory.class);
		PowerMockito.when(AgentManagedObjectFactory.getInstance(Mockito.any(), Mockito.any()))
				.thenReturn(mockManagedObjectFactory);

		InjectionUtil.inject(inventoryServiceImpl, mockPlatformService);

		// initialize agent managed object
		agentManagedObject = new AgentManagedObject();
		agentManagedObject.setName(DEVICE_NAME);
		agentManagedObject.setType(DEVICE_TYPE);
		agentManagedObject.setId("1");

		// initialize and inject configuration
		configuration = new InventoryServiceConfiguration();
		configuration.setAgent(true);
		configuration.setDevice(true);
		configuration.setDeviceName(DEVICE_NAME);
		configuration.setDeviceType(DEVICE_TYPE);

		// inject mocks "PlatformService", "SystemService" and configuration
		InjectionUtil.inject(inventoryServiceImpl, mockPlatformService);
        InjectionUtil.inject(inventoryServiceImpl, systemService);
        InjectionUtil.inject(inventoryServiceImpl, configuration);

		// behavior of mocked PlatformService
		when(mockPlatformService.getAgentManagedObject()).thenReturn(agentManagedObject);
		when(mockPlatformService.createAgentManagedObject(any(AgentManagedObject.class)))
				.thenReturn(agentManagedObject.getId());

		when(mockManagedObjectFactory.create()).thenReturn(agentManagedObject);
		when(mockManagedObjectFactory.create(DEVICE_NAME)).thenReturn(agentManagedObject);
	}

	/**
	 * test method start, no PlatformService injected
	 */
	@Test(expected = InventoryServiceException.class)
	public void testStartNoPlatformService() throws Exception {
	    InjectionUtil.inject(inventoryServiceImpl, "platformService", null);
		inventoryServiceImpl.start();
	}

	/**
	 * test method start, no SystemService injected
	 */
	@Test(expected = InventoryServiceException.class)
	public void testStartNoSystemService() throws Exception {
        InjectionUtil.inject(inventoryServiceImpl, "systemService", null);
		inventoryServiceImpl.start();
	}

	/**
	 * test method start
	 */
	@Test
	public void testStart() throws Exception {
		inventoryServiceImpl.start();
		assertTrue(inventoryServiceImpl.isStarted());
	}

	/**
	 * test method start, device is not registered
	 */
	@Test
	public void testIsDeviceRegisteredNotRegistered() throws Exception {
		// prepare mock
		when(mockPlatformService.isExternalIdAvailable()).thenReturn(false);

		// test isDeviceRegistered
		assertFalse(inventoryServiceImpl.isDeviceRegistered());
	}

	/**
	 * test method isDeviceRegistered, device is registered
	 */
	@Test
	public void testIsDeviceRegistered() throws Exception {
		// prepare mock
		when(mockPlatformService.isExternalIdAvailable()).thenReturn(true);

		// test isDeviceRegistered
		assertTrue(inventoryServiceImpl.isDeviceRegistered());
	}

	/**
	 * test method isDeviceRegistered, PlatformService.isExternalIdAvailable()
	 * throws 404 exception
	 */
	@Test
	public void testIsDeviceRegistered404() throws Exception {
		// prepare mock
		doThrow(new PlatformServiceException(404, "test", null)).when(mockPlatformService).isExternalIdAvailable();

		// test isDeviceRegistered
		assertFalse(inventoryServiceImpl.isDeviceRegistered());
	}

	/**
	 * test method isDeviceRegistered, PlatformService.getExternalId() throws
	 * exception (not 404)
	 */
	@Test(expected = InventoryServiceException.class)
	public void testIsDeviceRegisteredIsExternalIdAvailableException() throws Exception {
		// prepare mock
		doThrow(new PlatformServiceException(400, "test", null)).when(mockPlatformService).isExternalIdAvailable();

		// test isDeviceRegistered
		inventoryServiceImpl.isDeviceRegistered();
	}

	@Test
	public void testCreateAndRegisterDevice() throws Exception {
		// prepare inventoryService
		inventoryServiceImpl.start();

		inventoryServiceImpl.createAndRegisterDevice();
	}

	@Test(expected = InventoryServiceException.class)
	public void testCreateAndRegisterDeviceManagedObjectException() throws Exception {
		// prepare inventoryService
		inventoryServiceImpl.start();

		// prepare exception
		doThrow(new PlatformServiceException(500, "Exception", null)).when(mockPlatformService)
				.createAgentManagedObject(Mockito.any());
		inventoryServiceImpl.createAndRegisterDevice();
	}

	@Test(expected = InventoryServiceException.class)
	public void testCreateAndRegisterDeviceExternalIdException() throws Exception {
		// prepare inventoryService
		inventoryServiceImpl.start();

		// prepare exception
		doThrow(new PlatformServiceException(500, "Exception", null)).when(mockPlatformService)
				.createExternalId(Mockito.any());
		inventoryServiceImpl.createAndRegisterDevice();
	}

	@Test
	public void testUpdateDevice() throws Exception {
		inventoryServiceImpl.start();

		// prepare mock
		AgentManagedObject agentManagedObject = mock(AgentManagedObject.class);
		doReturn("test").when(agentManagedObject).getId();
		when(mockManagedObjectFactory.create()).thenReturn(agentManagedObject);

		// test
		inventoryServiceImpl.updateDevice();
	}

	@Test(expected = InventoryServiceException.class)
	public void testUpdateDeviceException() throws Exception {
		inventoryServiceImpl.start();

		// prepare mock
		when(mockPlatformService.getAgentManagedObject())
				.thenThrow(new PlatformServiceException(500, "Exception", null));

		// test
		inventoryServiceImpl.updateDevice();
	}
}
