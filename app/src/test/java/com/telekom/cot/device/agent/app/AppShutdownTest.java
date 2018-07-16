package com.telekom.cot.device.agent.app;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.alarm.AlarmService;
import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentShutdownException;
import com.telekom.cot.device.agent.common.exc.ConfigurationNotFoundException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.event.EventService;
import com.telekom.cot.device.agent.inventory.InventoryService;
import com.telekom.cot.device.agent.operation.OperationService;
import com.telekom.cot.device.agent.operation.OperationServiceConfiguration;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.sensor.SensorService;
import com.telekom.cot.device.agent.service.AgentServiceProvider;
import com.telekom.cot.device.agent.service.AgentServiceShutdownHelper;
import com.telekom.cot.device.agent.system.SystemService;


public class AppShutdownTest {

    private static final int SHUTDOWN_TIMEOUT = 100;  
    private static final int OPERATION_SERVICE_SHUTDOWN_TIMEOUT = 500;  
    
    @Mock private Logger mockLogger;
    @Mock private AgentServiceProvider mockAgentServiceProvider;
    @Mock private ConfigurationManager mockConfigurationManager;
    @Mock private AgentServiceShutdownHelper mockShutdownHelper;
    
    private CommonConfiguration commonConfiguration = new CommonConfiguration();
    private OperationServiceConfiguration operationServiceConfiguration = new OperationServiceConfiguration();
    private AgentShutdownException exception = new AgentShutdownException("can't shutdown");
    private AppShutdown appShutdown;
    
    @Before
    public void setUp() throws Exception {
        // init mock
        MockitoAnnotations.initMocks(this);

        // init configurations
        commonConfiguration.setShutdownTimeout(SHUTDOWN_TIMEOUT);
        operationServiceConfiguration.setShutdownTimeout(OPERATION_SERVICE_SHUTDOWN_TIMEOUT);
        
        // behavior of mocked ConfigurationManager
        when(mockConfigurationManager.getConfiguration(CommonConfiguration.class)).thenReturn(commonConfiguration);
        when(mockConfigurationManager.getConfiguration(OperationServiceConfiguration.class)).thenReturn(operationServiceConfiguration);

        // create AppShutdown instance and inject mockLogger and mockShutdownHelper
        appShutdown = new AppShutdown(mockAgentServiceProvider, mockConfigurationManager);
        InjectionUtil.injectStatic(AppShutdown.class, mockLogger);
        InjectionUtil.inject(appShutdown, mockShutdownHelper);
    }
    
    /**
     * test constructor, no AgentServiceProvider given
     */
    @Test(expected=AgentShutdownException.class)
    public void testConstructorAgentServiceProviderNull() throws Exception {
        new AppShutdown(null, mockConfigurationManager);
    }

    /**
     * test constructor, no ConfigurationManager given
     */
    @Test(expected=AgentShutdownException.class)
    public void testConstructorConfigurationManagerNull() throws Exception {
        new AppShutdown(mockAgentServiceProvider, null);
    }

    /**
     * test constructor, no CommonConfiguration found
     */
    @Test(expected=ConfigurationNotFoundException.class)
    public void testConstructorNoCommonConfiguration() throws Exception {
        doThrow(new ConfigurationNotFoundException("not found")).when(mockConfigurationManager).getConfiguration(CommonConfiguration.class);
        new AppShutdown(mockAgentServiceProvider, mockConfigurationManager);
    }

    /**
     * test constructor
     */
    @Test
    public void testConstructor() throws Exception {
        assertNotNull(new AppShutdown(mockAgentServiceProvider, mockConfigurationManager));
    }
    
    /**
     * test run, shutdown SensorService throws exception
     */
    @Test
    public void testRunShutdownSensorServiceException() throws Exception {
        doThrow(exception).when(mockShutdownHelper).shutdownService(SensorService.class, SHUTDOWN_TIMEOUT, true);
        appShutdown.run();
        
        verify(mockLogger).error("can't shut down sensor service and all sensor device services", exception);
    }
    
    /**
     * test run, shutdown AlarmService throws exception
     */
    @Test
    public void testRunShutdownAlarmServiceException() throws Exception {
        doThrow(exception).when(mockShutdownHelper).shutdownService(AlarmService.class, SHUTDOWN_TIMEOUT, true);
        appShutdown.run();
        
        verify(mockLogger).info("shut down sensor service and all sensor device services successfully");
        verify(mockLogger).error("can't shut down alarm service", exception);
    }
    
    /**
     * test run, shutdown EventService throws exception
     */
    @Test
    public void testRunShutdownEventServiceException() throws Exception {
        doThrow(exception).when(mockShutdownHelper).shutdownService(EventService.class, SHUTDOWN_TIMEOUT, true);
        appShutdown.run();
        
        verify(mockLogger).info("shut down sensor service and all sensor device services successfully");
        verify(mockLogger).info("shut down event alarm successfully");
        verify(mockLogger).error("can't shut down event service", exception);
    }

    /**
     * test run, OperationServiceConfiguration not found
     */
    @Test
    public void testRunOperationServiceConfigurationNotFound() throws Exception {
        ConfigurationNotFoundException e = new ConfigurationNotFoundException("not found");
        doThrow(e).when(mockConfigurationManager).getConfiguration(OperationServiceConfiguration.class);
        appShutdown.run();
        
        verify(mockLogger).info("shut down sensor service and all sensor device services successfully");
        verify(mockLogger).info("shut down event alarm successfully");
        verify(mockLogger).info("shut down event service successfully");
        verify(mockLogger).warn("can't get shutdown timeout from operation service configuration, use common timeout", e);
    }

    /**
     * test run, shutdown OperationService throws exception
     */
    @Test
    public void testRunShutdownOperationServiceException() throws Exception {
        doThrow(exception).when(mockShutdownHelper).shutdownService(OperationService.class, OPERATION_SERVICE_SHUTDOWN_TIMEOUT, true);
        appShutdown.run();
        
        verify(mockLogger).info("shut down sensor service and all sensor device services successfully");
        verify(mockLogger).info("shut down event alarm successfully");
        verify(mockLogger).info("shut down event service successfully");
        verify(mockLogger, never()).warn(eq("can't get shutdown timeout from operation service configuration, use common timeout"), any(AbstractAgentException.class));
        verify(mockLogger).error("can't shut down operation service and all operation handlers", exception);
    }

    /**
     * test run, shutdown InventoryService throws exception
     */
    @Test
    public void testRunShutdownInventoryServiceException() throws Exception {
        doThrow(exception).when(mockShutdownHelper).shutdownService(InventoryService.class, SHUTDOWN_TIMEOUT, true);
        appShutdown.run();
        
        verify(mockLogger).info("shut down sensor service and all sensor device services successfully");
        verify(mockLogger).info("shut down event alarm successfully");
        verify(mockLogger).info("shut down event service successfully");
        verify(mockLogger).info("shut down operation service and all operation handlers successfully");
        verify(mockLogger).error("can't shut down inventory service", exception);
    }

    /**
     * test run, shutdown PlatformService throws exception
     */
    @Test
    public void testRunShutdownPlatformServiceException() throws Exception {
        doThrow(exception).when(mockShutdownHelper).shutdownService(PlatformService.class, SHUTDOWN_TIMEOUT, true);
        appShutdown.run();
        
        verify(mockLogger).info("shut down sensor service and all sensor device services successfully");
        verify(mockLogger).info("shut down event alarm successfully");
        verify(mockLogger).info("shut down event service successfully");
        verify(mockLogger).info("shut down operation service and all operation handlers successfully");
        verify(mockLogger).info("shut down inventory service successfully");
        verify(mockLogger).error("can't shut down platform service", exception);
    }

    /**
     * test run, shutdown SystemService throws exception
     */
    @Test
    public void testRunShutdownSystemServiceException() throws Exception {
        doThrow(exception).when(mockShutdownHelper).shutdownService(SystemService.class, SHUTDOWN_TIMEOUT, true);
        appShutdown.run();
        
        verify(mockLogger).info("shut down sensor service and all sensor device services successfully");
        verify(mockLogger).info("shut down event alarm successfully");
        verify(mockLogger).info("shut down event service successfully");
        verify(mockLogger).info("shut down operation service and all operation handlers successfully");
        verify(mockLogger).info("shut down inventory service successfully");
        verify(mockLogger).info("shut down platform service successfully");
        verify(mockLogger).error("can't shut down system service", exception);
    }

    /**
     * test run
     */
    @Test
    public void testRun() throws Exception {
        appShutdown.run();
        
        verify(mockLogger).info("shut down sensor service and all sensor device services successfully");
        verify(mockLogger).info("shut down event alarm successfully");
        verify(mockLogger).info("shut down event service successfully");
        verify(mockLogger).info("shut down operation service and all operation handlers successfully");
        verify(mockLogger).info("shut down inventory service successfully");
        verify(mockLogger).info("shut down platform service successfully");
        verify(mockLogger).info("shut down system service successfully");
    }
}
