package com.telekom.cot.device.agent.credentials;

import static org.junit.Assert.*;

import com.telekom.cot.device.agent.common.util.ValidationUtil;
import com.telekom.cot.device.agent.service.configuration.AgentCredentials;
import org.junit.Before;
import org.junit.Test;

public class DeviceCredentialsServiceConfigurationTest {

    private static final int INTERVAL = 1;
    private DeviceCredentialsServiceConfiguration deviceCredentialsServiceConfiguration;
    private AgentCredentials bootstrapCredentials;
    private DeviceCredentialsServiceConfiguration.DeviceIdTemplates deviceIdTemplates;


    @Before
    public void setup() {
        bootstrapCredentials = new AgentCredentials();
        bootstrapCredentials.setUsername("test");
        bootstrapCredentials.setTenant("test");
        bootstrapCredentials.setPassword("test");

        deviceIdTemplates = DeviceCredentialsServiceConfiguration.DeviceIdTemplates.EXTERNAL_ID_VALUE;

        deviceCredentialsServiceConfiguration = new com.telekom.cot.device.agent.credentials.DeviceCredentialsServiceConfiguration();
        deviceCredentialsServiceConfiguration.setInterval(INTERVAL);
        deviceCredentialsServiceConfiguration.setDeviceIdTemplate(deviceIdTemplates);
        deviceCredentialsServiceConfiguration.setBootstrapCredentials(bootstrapCredentials);
    }

    @Test
    public void testSettersAndGetters() {
        deviceCredentialsServiceConfiguration.setBootstrapCredentials(bootstrapCredentials);
        deviceCredentialsServiceConfiguration.setDeviceIdTemplate(deviceIdTemplates);
        deviceCredentialsServiceConfiguration.setInterval(5);

        assertSame(bootstrapCredentials, deviceCredentialsServiceConfiguration.getBootstrapCredentials());
        assertSame(deviceIdTemplates, deviceCredentialsServiceConfiguration.getDeviceIdTemplate());
        assertEquals(5, deviceCredentialsServiceConfiguration.getInterval());
    }

    @Test
    public void validate() {
        assertTrue(ValidationUtil.isValid(deviceCredentialsServiceConfiguration));
    }

    @Test
    public void bootstrapCredentialsNull() {
        deviceCredentialsServiceConfiguration.setBootstrapCredentials(null);

        assertFalse(ValidationUtil.isValid(deviceCredentialsServiceConfiguration));
    }

    @Test
    public void deviceIdTemplateNull() {
        deviceCredentialsServiceConfiguration.setDeviceIdTemplate(null);

        assertFalse(ValidationUtil.isValid(deviceCredentialsServiceConfiguration));
    }
}
