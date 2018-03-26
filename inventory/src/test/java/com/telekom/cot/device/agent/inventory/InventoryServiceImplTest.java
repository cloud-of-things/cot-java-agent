package com.telekom.cot.device.agent.inventory;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.telekom.m2m.cot.restsdk.library.Fragment;
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
import com.telekom.cot.device.agent.common.exc.AgentServiceNotFoundException;
import com.telekom.cot.device.agent.common.exc.ConfigurationNotFoundException;
import com.telekom.cot.device.agent.common.exc.InventoryServiceException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.common.exc.PropertyNotFoundException;
import com.telekom.cot.device.agent.common.util.InjectionUtil;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.service.AgentServiceProvider;
import com.telekom.cot.device.agent.service.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.service.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.system.SystemService;
import com.telekom.cot.device.agent.system.properties.Properties;
import com.telekom.m2m.cot.restsdk.identity.ExternalId;
import com.telekom.m2m.cot.restsdk.inventory.ManagedObject;
import com.telekom.m2m.cot.restsdk.library.devicemanagement.SupportedOperations;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ManagedObjectFactory.class)
public class InventoryServiceImplTest {

    private static final String DEVICE_EXTERNAL_ID = "testDeviceExternalId";
    private static final String DEVICE_EXTERNAL_TYPE = "testDeviceExternalType";
    private static final String DEVICE_TYPE = "testDeviceType";
    private static final String DEVICE_NAME = "testDevice";

    @Mock private AgentServiceProvider mockServiceProvider;
	@Mock private ConfigurationManager mockConfigurationManager;
    @Mock private PlatformService mockPlatformService;
    @Mock private ManagedObjectFactory mockManagedObjectFactory;

    private ManagedObject managedObject;
    private ExternalId externalId;
    private InventoryServiceConfiguration config;
    private SystemService systemService;
    private SupportedOperations supportedOperations;

    private InventoryServiceImpl inventoryServiceImpl = new InventoryServiceImpl();

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    @Before
    public void setUp() throws Exception {

        supportedOperations = new SupportedOperations("c8y_Restart");
        
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
			public void init(AgentServiceProvider serviceProvider, ConfigurationManager configurationManager,
					AgentCredentialsManager agentCredentialsManager) throws AbstractAgentException {
			}
        };

        // initialize mocks
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(ManagedObjectFactory.class);
        PowerMockito.when(ManagedObjectFactory.getInstance(Mockito.any(), Mockito.any())).thenReturn(mockManagedObjectFactory);
        
        InjectionUtil.inject(inventoryServiceImpl, mockPlatformService);
        InjectionUtil.inject(inventoryServiceImpl, mockConfigurationManager);

        // initialize managed object
        managedObject = new ManagedObject();
        managedObject.setName(DEVICE_NAME);
        managedObject.setType(DEVICE_TYPE);
        managedObject.setId("1");

        // init externalId
        externalId = new ExternalId();
        externalId.setExternalId(DEVICE_EXTERNAL_ID);
        externalId.setType(DEVICE_EXTERNAL_TYPE);

        // initialize configuration
        config = new InventoryServiceConfiguration();
        config.setAgent(true);
        config.setDevice(true);
        config.setDeviceName(DEVICE_NAME);
        config.setDeviceType(DEVICE_TYPE);

        // inject mock "AgentServiceProvider" and configuration
        InjectionUtil.inject(inventoryServiceImpl, mockServiceProvider);

        // return values of AgentServiceProvider
        when(mockServiceProvider.getService(PlatformService.class)).thenReturn(mockPlatformService);
        when(mockServiceProvider.getService(SystemService.class)).thenReturn(systemService);

        when(mockConfigurationManager.getConfiguration(InventoryServiceConfiguration.class)).thenReturn(config);

        // behavior of mocked PlatformService
        when(mockPlatformService.getManagedObject()).thenReturn(managedObject);
        when(mockPlatformService.createManagedObject(any(ManagedObject.class))).thenReturn(managedObject);
        when(mockPlatformService.createExternalId(any(String.class))).thenReturn(externalId);

        when(mockManagedObjectFactory.create()).thenReturn(managedObject);
        when(mockManagedObjectFactory.create(DEVICE_NAME)).thenReturn(managedObject);
    }

    /**
     * test method start, getService(PlatformService.class) throws exception
     */
    @Test(expected = AgentServiceNotFoundException.class)
    public void testStartNoPlatformService() throws Exception {
        doThrow(new AgentServiceNotFoundException("test")).when(mockServiceProvider).getService(PlatformService.class);

        inventoryServiceImpl.start();
    }

    /**
     * test method start, getConfigurationManager().getConfiguration(InventoryServiceConfiguration.class) throws exception
     */
    @Test(expected = ConfigurationNotFoundException.class)
    public void testStartNoConfiguration() throws Exception {
        doThrow(new ConfigurationNotFoundException("not found")).when(mockConfigurationManager).getConfiguration(InventoryServiceConfiguration.class);

        inventoryServiceImpl.start();
    }

    /**
     * test method start, getService(SystemService.class) throws exception
     */
    @Test(expected = AgentServiceNotFoundException.class)
    public void testStartNoSystemService() throws Exception {
        doThrow(new AgentServiceNotFoundException("test")).when(mockServiceProvider).getService(SystemService.class);

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
        when(mockPlatformService.getExternalId()).thenReturn(null);

        // test isDeviceRegistered
        assertFalse(inventoryServiceImpl.isDeviceRegistered());
    }

    /**
     * test method isDeviceRegistered, device is registered
     */
    @Test
    public void testIsDeviceRegistered() throws Exception {
        // prepare mock
        when(mockPlatformService.getExternalId()).thenReturn(externalId);

        // test isDeviceRegistered
        assertTrue(inventoryServiceImpl.isDeviceRegistered());
    }

    /**
     * test method isDeviceRegistered, PlatformService.getExternalId() throws 404 exception
     */
    @Test
    public void testIsDeviceRegistered404() throws Exception {
        // prepare mock
        doThrow(new PlatformServiceException(404, "test", null)).when(mockPlatformService).getExternalId();

        // test isDeviceRegistered
        assertFalse(inventoryServiceImpl.isDeviceRegistered());
    }

    /**
     * test method isDeviceRegistered, PlatformService.getExternalId() throws exception (not 404)
     */
    @Test(expected=InventoryServiceException.class)
    public void testIsDeviceRegisteredGetExternalIdException() throws Exception {
        // prepare mock
        doThrow(new PlatformServiceException(400, "test", null)).when(mockPlatformService).getExternalId();

        // test isDeviceRegistered
        inventoryServiceImpl.isDeviceRegistered();
    }

    @Test
    public void testUpdateFragment() throws Exception {
        // test update
        inventoryServiceImpl.update(supportedOperations);
        // verify
        verify(mockPlatformService).updateSupportedOperations(supportedOperations);
    }

    @Test(expected = InventoryServiceException.class)
    public void testUpdateFragmentException() throws Exception {
        // mock fragment
        Fragment fragment = mock(Fragment.class);

        // test update
        inventoryServiceImpl.update(fragment);
    }


    @Test
    public void testCreateAndRegisterDevice() throws Exception {
        //prepare inventoryService
        inventoryServiceImpl.start();

        inventoryServiceImpl.createAndRegisterDevice();
    }

    @Test(expected = InventoryServiceException.class)
    public void testCreateAndRegisterDeviceManagedObjectException() throws Exception {
        //prepare inventoryService
        inventoryServiceImpl.start();

        //prepare exception
        doThrow(new PlatformServiceException(500, "Exception", null)).when(mockPlatformService).createManagedObject(Mockito.any());
        inventoryServiceImpl.createAndRegisterDevice();
    }

    @Test(expected = InventoryServiceException.class)
    public void testCreateAndRegisterDeviceExternalIdException() throws Exception {
        //prepare inventoryService
        inventoryServiceImpl.start();

        //prepare exception
        doThrow(new PlatformServiceException(500, "Exception", null)).when(mockPlatformService).createExternalId(Mockito.any());
        inventoryServiceImpl.createAndRegisterDevice();
    }

    @Test
    public void testUpdateDevice() throws Exception {
        inventoryServiceImpl.start();

        //prepare mock
        ManagedObject managedObject = mock(ManagedObject.class);
        doReturn("test").when(managedObject).getId();
        when(mockManagedObjectFactory.create()).thenReturn(managedObject);


        //test
        inventoryServiceImpl.updateDevice();
    }

    @Test(expected = InventoryServiceException.class)
    public void testUpdateDeviceException() throws Exception {
        inventoryServiceImpl.start();

        //prepare mock
        when(mockPlatformService.getManagedObject()).thenThrow(new PlatformServiceException(500, "Exception", null));

        //test
        inventoryServiceImpl.updateDevice();
    }
}
