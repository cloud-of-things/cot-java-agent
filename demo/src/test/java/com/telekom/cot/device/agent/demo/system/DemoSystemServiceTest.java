package com.telekom.cot.device.agent.demo.system;

import static org.mockito.Mockito.when;

import java.net.URL;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.util.InjectionUtil;
import com.telekom.cot.device.agent.service.AgentServiceProvider;
import com.telekom.cot.device.agent.service.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.service.configuration.ConfigurationManagerImpl;
import com.telekom.cot.device.agent.service.configuration.MobilePropertiesConfiguration;
import com.telekom.cot.device.agent.system.SystemService;

public class DemoSystemServiceTest {

    @Mock
    private ConfigurationManager mockConfigurationManager;
    @Mock
    private DemoHardwareProperties mockDemoHardwareProperties;
    @Mock
    private MobilePropertiesConfiguration mockMobilePropertiesConfiguration;
    @Mock
    private DemoFirmwareProperties mockDemoFirmwareProperties;
    @Mock
    private AgentServiceProvider mockServiceProvider;
    @Mock
    private SystemService mockSystemService;
    private DemoSystemService demoSystemService = new DemoSystemService();

    @Before
    public void setUp() throws AbstractAgentException {
        // initialize mocks
        MockitoAnnotations.initMocks(this);
        InjectionUtil.inject(demoSystemService, mockConfigurationManager);
        InjectionUtil.inject(demoSystemService, mockServiceProvider);
        // behavior of mocked ConfigurationManager
        when(mockConfigurationManager.getConfiguration(DemoHardwareProperties.class))
                        .thenReturn(mockDemoHardwareProperties);
        when(mockConfigurationManager.getConfiguration(MobilePropertiesConfiguration.class))
                        .thenReturn(mockMobilePropertiesConfiguration);
        when(mockConfigurationManager.getConfiguration(DemoFirmwareProperties.class))
                        .thenReturn(mockDemoFirmwareProperties);
        // behavior of mocked ServiceProvider
        when(mockServiceProvider.getService(SystemService.class)).thenReturn(mockSystemService);
    }

    @Test
    public void testDemoHardwareProperties() throws AbstractAgentException {
        // child
        URL configurationFileURL = DemoSystemServiceTest.class.getResource("/system_test_child_props_agent.yaml");
        ConfigurationManager manager = ConfigurationManagerImpl.getInstance(configurationFileURL.getFile());

        DemoHardwareProperties demoHardwareProperties = manager.getConfiguration(DemoHardwareProperties.class);
        Assert.assertThat(demoHardwareProperties.getModel(), Matchers.equalTo(null));
        Assert.assertThat(demoHardwareProperties.getRevision(), Matchers.equalTo(null));
        Assert.assertThat(demoHardwareProperties.getSerialNumber(), Matchers.equalTo(null));
        
        // root
        configurationFileURL = DemoSystemServiceTest.class.getResource("/system_test_root_props_agent.yaml");
        manager = ConfigurationManagerImpl.getInstance(configurationFileURL.getFile());
        try {
            manager.getConfiguration(DemoHardwareProperties.class);
            Assert.fail();
        } catch (AbstractAgentException exc) {
            // ignore
        }

        // root
        configurationFileURL = DemoSystemServiceTest.class.getResource("/system_test_missing_root_props_agent.yaml");
        manager = ConfigurationManagerImpl.getInstance(configurationFileURL.getFile());
        try {
            manager.getConfiguration(DemoHardwareProperties.class);
            Assert.fail();
        } catch (AbstractAgentException exc) {
            // ignore
        }
    }

    @Test
    public void testStart() throws AbstractAgentException {
        demoSystemService.start();
    }
}
