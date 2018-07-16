package com.telekom.cot.device.agent.platform.mqtt;

import static org.mockito.Mockito.*;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.telekom.cot.device.agent.common.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.common.exc.AgentServiceNotFoundException;
import com.telekom.cot.device.agent.common.exc.ConfigurationNotFoundException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.service.AgentService;
import com.telekom.cot.device.agent.service.AgentServiceManager;
import com.telekom.cot.device.agent.service.AgentServiceManagerImpl;
import com.telekom.cot.device.agent.system.SystemService;

public class PlatformServiceMqttImplConfTest {

    @Mock
    private ServiceLoader<AgentService> mockServiceLoader;
    @Mock
    private AgentCredentialsManager mockAgentCredentialsManager;
    @Mock
    private Iterator<AgentService> mockServices;
    @Mock
    private ConfigurationManager mockConfigurationManager;
    @Mock
    private PlatformServiceMqttConfiguration mockConfiguration;
    @Mock
    private SystemService mockSystemService;
    
    private AgentServiceManager agentServiceManager;
    private PlatformService platformService;

    @Before
    public void setUp() throws Exception {
        // service manager instance        
        agentServiceManager = AgentServiceManagerImpl.getInstance();
        
        // test platform
        platformService = new PlatformServiceMqttImpl();
        
        /* --- init mocks -- */
        MockitoAnnotations.initMocks(this);
        InjectionUtil.inject(agentServiceManager, mockServiceLoader);
        
        /* --- define behavior -- */
        when(mockServiceLoader.iterator()).thenReturn(mockServices);
        when(mockServices.hasNext()).thenReturn(true, true, false);
        when(mockServices.next()).thenReturn(platformService, mockSystemService);
        when(mockConfigurationManager.getConfiguration(PlatformServiceMqttConfiguration.class)).thenReturn(mockConfiguration);
    }

    @Test
    public void testLoadAndInitServices() throws Exception {
        agentServiceManager.loadAndInitServices(mockConfigurationManager, mockAgentCredentialsManager);
        agentServiceManager.getService(PlatformService.class);
    }

    @Test(expected=AgentServiceNotFoundException.class)
    public void testLoadAndInitServicesError() throws Exception {
        doThrow(new ConfigurationNotFoundException("test")).when(mockConfigurationManager).getConfiguration(PlatformServiceMqttConfiguration.class);
        agentServiceManager.loadAndInitServices(mockConfigurationManager, mockAgentCredentialsManager);
        agentServiceManager.getService(PlatformService.class);
    }
}
