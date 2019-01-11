package com.telekom.cot.device.agent.app;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.configuration.AgentCredentials;
import com.telekom.cot.device.agent.common.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.credentials.DeviceCredentialsService;
import com.telekom.cot.device.agent.event.EventService;
import com.telekom.cot.device.agent.inventory.InventoryService;
import com.telekom.cot.device.agent.measurement.MeasurementService;
import com.telekom.cot.device.agent.operation.OperationService;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.service.AgentServiceManager;
import com.telekom.cot.device.agent.system.SystemService;

public class AppBootstrapFactoryTest {

    private static final String EVENT_AGENT_STARTED_TYPE = "com_telekom_cot_device_agent_AgentStarted";
    private static final String EVENT_AGENT_STARTED_TEXT = "Agent was started successfully";
    private static final String EVENT_AGENT_STARTUP = "c8y_EventStartup";
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
    private MeasurementService mockMeasurementService;
    @Mock
    private EventService mockEventService;
    @Mock
    private DeviceCredentialsService mockCredentialsService;

    private CommonConfiguration commonConfiguration;
    
    //
    private AppBootstrapSteps steps = null;

    @Before
    public void setUp() throws Exception {
        // init mocks
        MockitoAnnotations.initMocks(this);
        
        // init common configuration, behavior of mocked configuration manager
        commonConfiguration = new CommonConfiguration();
        commonConfiguration.setConnectivityTimeout(0);
        commonConfiguration.setShutdownTimeout(2000);
        when(mockConfigurationManager.getConfiguration(CommonConfiguration.class)).thenReturn(commonConfiguration);
        
        // create factory
        steps = AppBootstrapFactory.getInstance(mockAgentServiceManager, mockConfigurationManager, mockAgentCredentialsManager);

        // inject
        InjectionUtil.injectStatic(AppBootstrap.class, mockLogger);

        // behavior of mocked AgentServiceManager
        when(mockAgentServiceManager.getService(SystemService.class)).thenReturn(mockSystemService);
        when(mockAgentServiceManager.getService(PlatformService.class)).thenReturn(mockPlatformService);
        when(mockAgentServiceManager.getService(InventoryService.class)).thenReturn(mockInventoryService);
        when(mockAgentServiceManager.getService(OperationService.class)).thenReturn(mockOperationService);
        when(mockAgentServiceManager.getService(MeasurementService.class)).thenReturn(mockMeasurementService);
        when(mockAgentServiceManager.getService(EventService.class)).thenReturn(mockEventService);
        when(mockAgentServiceManager.getService(DeviceCredentialsService.class)).thenReturn(mockCredentialsService);
    }

    /*
     * credentialsAvailable
     */
    @Test
    public void testCredentialsAvailable() throws AppMainException, AbstractAgentException {
        when(mockAgentCredentialsManager.getCredentials()).thenReturn(new AgentCredentials());
        assertThat(steps.credentialsAvailable(), Matchers.equalTo(true));
    }

    @Test
    public void testCredentialsAvailableExc() throws AppMainException, AbstractAgentException {
        doThrow(new TestAgentException()).when(mockAgentCredentialsManager).getCredentials();
        assertThat(steps.credentialsAvailable(), Matchers.equalTo(false));
    }

    /*
     * requestAndWriteDeviceCredentials
     */
    @Test
    public void testRequestAndWriteDeviceCredentials() throws AppMainException, AbstractAgentException {
        when(mockCredentialsService.isStarted()).thenAnswer(new Answer<Boolean>() {

            private Boolean started = Boolean.TRUE;

            public Boolean answer(InvocationOnMock invocation) {
                started = !started;
                return started;
            }
        });
        steps.requestAndWriteDeviceCredentials();
        verify(mockCredentialsService).start();
        verify(mockCredentialsService).stop();
        verify(mockCredentialsService).requestAndWriteDeviceCredentials();
        verify(mockAgentServiceManager, times(4)).getService(DeviceCredentialsService.class);
    }

    @Test
    public void testRequestAndWriteDeviceCredentialsExc() throws AppMainException, AbstractAgentException {
        doThrow(new TestAgentException()).when(mockCredentialsService).requestAndWriteDeviceCredentials();
        try {
            steps.requestAndWriteDeviceCredentials();
            fail();
        } catch (AppMainException exception) {
            assertThat(exception.getMessage(), Matchers.equalTo("can't request device credentials"));
        }
    }

    /*
     * isDeviceRegistered
     */
    @Test
    public void testIsDeviceRegistered() throws AppMainException, AbstractAgentException {
        when(mockInventoryService.isDeviceRegistered()).thenReturn(true);
        assertThat(steps.isDeviceRegistered(), Matchers.equalTo(true));
    }

    @Test
    public void testIsDeviceRegisteredExc() throws AppMainException, AbstractAgentException {
        doThrow(new TestAgentException()).when(mockInventoryService).isDeviceRegistered();
        try {
            steps.isDeviceRegistered();
            fail();
        } catch (AppMainException exception) {
            // ignore
        }
    }

    /*
     * updateDevice
     */
    @Test
    public void testUpdateDevice() throws AppMainException, AbstractAgentException {
        steps.updateDevice();
        verify(mockInventoryService).updateDevice();
    }

    @Test
    public void testUpdateDeviceExc() throws AppMainException, AbstractAgentException {
        doThrow(new TestAgentException()).when(mockInventoryService).updateDevice();
        try {
            steps.updateDevice();
            fail();
        } catch (AppMainException exception) {
            // ignore
        }
    }

    /*
     * createAndRegisterDevice
     */
    @Test
    public void testCreateAndRegisterDevice() throws AppMainException, AbstractAgentException {
        steps.createAndRegisterDevice();
        verify(mockInventoryService).createAndRegisterDevice();
    }

    @Test
    public void testCreateAndRegisterDeviceExc() throws AppMainException, AbstractAgentException {
        doThrow(new TestAgentException()).when(mockInventoryService).createAndRegisterDevice();
        try {
            steps.createAndRegisterDevice();
            fail();
        } catch (AppMainException exception) {
            // ignore
        }
    }

    /*
     * startService
     */
    @Test
    public void testStartServiceIsStartedFalse() throws AppMainException, AbstractAgentException {
        when(mockAgentServiceManager.getService(InventoryService.class)).thenReturn(mockInventoryService);
        when(mockInventoryService.isStarted()).thenReturn(false);
        steps.startService(InventoryService.class);
        verify(mockInventoryService, times(1)).start();
    }

    @Test
    public void testStartServiceIsStartedTrue() throws AppMainException, AbstractAgentException {
        when(mockAgentServiceManager.getService(InventoryService.class)).thenReturn(mockInventoryService);
        when(mockInventoryService.isStarted()).thenReturn(true);
        steps.startService(InventoryService.class);
        verify(mockInventoryService, times(0)).start();
    }

    @Test
    public void testStartServiceExc() throws AppMainException, AbstractAgentException {
        CommonConfiguration cc = new CommonConfiguration();
        cc.setShutdownTimeout(10);
        doThrow(new TestAgentException()).when(mockInventoryService).start();
        when(mockConfigurationManager.getConfiguration(CommonConfiguration.class)).thenReturn(cc);
        try {
            steps.startService(SystemService.class);
            steps.startService(InventoryService.class);
            fail();
        } catch (AppMainException exception) {
            // ignore
        }
        verify(mockSystemService).stop();
        verify(mockInventoryService).stop();
    }

    /*
     * stopService
     */
    @Test
    public void testStopServiceIsStartedFalse() throws AppMainException, AbstractAgentException {
        when(mockAgentServiceManager.getService(InventoryService.class)).thenReturn(mockInventoryService);
        when(mockInventoryService.isStarted()).thenReturn(false);
        steps.stopService(InventoryService.class);
        verify(mockInventoryService, times(0)).stop();
    }

    @Test
    public void testStopServiceIsStartedTrue() throws AppMainException, AbstractAgentException {
        when(mockAgentServiceManager.getService(InventoryService.class)).thenReturn(mockInventoryService);
        when(mockInventoryService.isStarted()).thenReturn(true);
        steps.stopService(InventoryService.class);
        verify(mockInventoryService, times(1)).stop();
    }

    @Test
    public void testStopServiceExc() throws AppMainException, AbstractAgentException {
        doThrow(new TestAgentException()).when(mockAgentServiceManager).getService(InventoryService.class);
        try {
            steps.stopService(InventoryService.class);
            fail();
        } catch (AppMainException exception) {
            // ignore
        }
    }

    /*
     * sendEventAgentStarted
     */
    @Test
    public void testSendEventAgentStarted() throws AppMainException, AbstractAgentException {
        steps.sendEventAgentStarted();
        verify(mockEventService, times(1)).createEvent(EVENT_AGENT_STARTED_TYPE, EVENT_AGENT_STARTED_TEXT, EVENT_AGENT_STARTUP);
    }

    static class TestAgentException extends AbstractAgentException {

        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = 1L;

        public TestAgentException() {
            super("test");
        }
    }
}
