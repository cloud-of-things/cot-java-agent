package com.telekom.cot.device.agent.app;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.telekom.cot.device.agent.alarm.AlarmService;
import com.telekom.cot.device.agent.common.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.event.EventService;
import com.telekom.cot.device.agent.inventory.InventoryService;
import com.telekom.cot.device.agent.operation.OperationService;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.sensor.SensorService;
import com.telekom.cot.device.agent.service.AgentServiceManager;
import com.telekom.cot.device.agent.system.SystemService;

public class AppBootstrapTest {

    @Mock
    private AgentServiceManager mockServiceManager;
    @Mock
    private ConfigurationManager mockConfigurationManager;
    @Mock
    private AgentCredentialsManager mockAgentCredentialsManager;
    @Mock
    private AppBootstrapSteps mockSteps;

    private CommonConfiguration commonConfiguration;
    private AppBootstrap appBootstrap;

    @Before
    public void setUp() throws Exception {
        // init mocks
        MockitoAnnotations.initMocks(this);

        // init common configuration, behavior of mocked configuration manager
        commonConfiguration = new CommonConfiguration();
        commonConfiguration.setConnectivityTimeout(0);
        commonConfiguration.setShutdownTimeout(2000);
        when(mockConfigurationManager.getConfiguration(CommonConfiguration.class)).thenReturn(commonConfiguration);
        
        appBootstrap = AppBootstrap.getInstance(mockServiceManager, mockConfigurationManager, mockAgentCredentialsManager);
        MockitoAnnotations.initMocks(this);
        InjectionUtil.inject(appBootstrap, mockSteps);
    }

    @Test
    public void testNoCredentialsAndNoRegistration() throws AppMainException {
        when(mockSteps.credentialsAvailable()).thenReturn(false);
        when(mockSteps.isDeviceRegistered()).thenReturn(false);
        appBootstrap.start();
        // verify
        verify(mockSteps).startService(SystemService.class);
        // credentials
        verify(mockSteps).requestAndWriteDeviceCredentials();
        verify(mockSteps).startService(PlatformService.class);
        verify(mockSteps).startService(InventoryService.class);
        // create and register new device
        verify(mockSteps).createAndRegisterDevice();
        verify(mockSteps).startService(OperationService.class);
        verify(mockSteps).startService(EventService.class);
        verify(mockSteps).startService(AlarmService.class);
        verify(mockSteps).startService(SensorService.class);
        verify(mockSteps).sendEventAgentStarted();
    }

    @Test
    public void testCredentialsAndNoRegistration() throws AppMainException {
        when(mockSteps.credentialsAvailable()).thenReturn(true);
        when(mockSteps.isDeviceRegistered()).thenReturn(false);
        appBootstrap.start();
        // verify
        verify(mockSteps).startService(SystemService.class);
        verify(mockSteps).startService(PlatformService.class);
        verify(mockSteps).startService(InventoryService.class);
        // create and register new device
        verify(mockSteps).createAndRegisterDevice();
        verify(mockSteps).startService(OperationService.class);
        verify(mockSteps).startService(EventService.class);
        verify(mockSteps).startService(AlarmService.class);
        verify(mockSteps).startService(SensorService.class);
        verify(mockSteps).sendEventAgentStarted();
    }

    @Test
    public void testNoCredentialsAndRegistration() throws AppMainException {
        when(mockSteps.credentialsAvailable()).thenReturn(false);
        when(mockSteps.isDeviceRegistered()).thenReturn(true);
        appBootstrap.start();
        // verify
        verify(mockSteps).startService(SystemService.class);
        // credentials
        verify(mockSteps).requestAndWriteDeviceCredentials();
        verify(mockSteps).startService(PlatformService.class);
        verify(mockSteps).startService(InventoryService.class);
        verify(mockSteps).startService(OperationService.class);
        verify(mockSteps).startService(EventService.class);
        verify(mockSteps).startService(AlarmService.class);
        verify(mockSteps).startService(SensorService.class);
        verify(mockSteps).sendEventAgentStarted();
    }

    @Test
    public void testCredentialsAndRegistration() throws AppMainException {
        when(mockSteps.credentialsAvailable()).thenReturn(false);
        when(mockSteps.isDeviceRegistered()).thenReturn(false);
        appBootstrap.start();
        // verify
        verify(mockSteps).startService(SystemService.class);
        verify(mockSteps).startService(PlatformService.class);
        verify(mockSteps).startService(InventoryService.class);
        verify(mockSteps).startService(OperationService.class);
        verify(mockSteps).startService(EventService.class);
        verify(mockSteps).startService(AlarmService.class);
        verify(mockSteps).startService(SensorService.class);
        verify(mockSteps).sendEventAgentStarted();
    }
}
