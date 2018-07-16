package com.telekom.cot.device.agent.demo.system;

import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.file.Paths;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.common.configuration.ConfigurationManagerImpl;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.service.AgentServiceProvider;
import com.telekom.cot.device.agent.system.SystemService;

public class DemoSystemServiceTest {
    @Mock
    private DemoHardwareProperties mockDemoHardwareProperties;
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
        InjectionUtil.inject(demoSystemService, mockDemoHardwareProperties);
        InjectionUtil.inject(demoSystemService, mockDemoFirmwareProperties);
        InjectionUtil.inject(demoSystemService, mockServiceProvider);

        // behavior of mocked ServiceProvider
        when(mockServiceProvider.getService(SystemService.class)).thenReturn(mockSystemService);
    }

    @Test
    public void testDemoHardwareProperties() throws Exception {
        // child
        URI configurationFileURI = DemoSystemServiceTest.class.getResource("/system_test_child_props_agent.yaml").toURI();
        ConfigurationManager manager = ConfigurationManagerImpl.getInstance(Paths.get(configurationFileURI));

        DemoHardwareProperties demoHardwareProperties = manager.getConfiguration(DemoHardwareProperties.class);
        Assert.assertThat(demoHardwareProperties.getModel(), Matchers.equalTo(null));
        Assert.assertThat(demoHardwareProperties.getRevision(), Matchers.equalTo(null));
        Assert.assertThat(demoHardwareProperties.getSerialNumber(), Matchers.equalTo(null));
        
        // root
        configurationFileURI = DemoSystemServiceTest.class.getResource("/system_test_root_props_agent.yaml").toURI();
        manager = ConfigurationManagerImpl.getInstance(Paths.get(configurationFileURI));
        try {
            manager.getConfiguration(DemoHardwareProperties.class);
            Assert.fail();
        } catch (AbstractAgentException exc) {
            // ignore
        }

        // root
        configurationFileURI = DemoSystemServiceTest.class.getResource("/system_test_missing_root_props_agent.yaml").toURI();
        manager = ConfigurationManagerImpl.getInstance(Paths.get(configurationFileURI));
        try {
            manager.getConfiguration(DemoHardwareProperties.class);
            Assert.fail();
        } catch (AbstractAgentException exc) {
            // ignore
        }
    }
    
    @Test
    public void testDemoFirmwareProperties() throws Exception {
        // child
        URI configurationFileURI = DemoSystemServiceTest.class.getResource("/system_test_child_props_agent.yaml").toURI();
        ConfigurationManager manager = ConfigurationManagerImpl.getInstance(Paths.get(configurationFileURI));

        DemoFirmwareProperties demoHardwareProperties = manager.getConfiguration(DemoFirmwareProperties.class);
        Assert.assertThat(demoHardwareProperties.getName(), Matchers.equalTo(null));
        Assert.assertThat(demoHardwareProperties.getVersion(), Matchers.equalTo(null));
        Assert.assertThat(demoHardwareProperties.getUrl(), Matchers.equalTo(null));
        
        // root
        configurationFileURI = DemoSystemServiceTest.class.getResource("/system_test_root_props_agent.yaml").toURI();
        manager = ConfigurationManagerImpl.getInstance(Paths.get(configurationFileURI));
        try {
            manager.getConfiguration(DemoFirmwareProperties.class);
            Assert.fail();
        } catch (AbstractAgentException exc) {
            // ignore
        }

        // root
        configurationFileURI = DemoSystemServiceTest.class.getResource("/system_test_missing_root_props_agent.yaml").toURI();
        manager = ConfigurationManagerImpl.getInstance(Paths.get(configurationFileURI));
        try {
            manager.getConfiguration(DemoFirmwareProperties.class);
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
