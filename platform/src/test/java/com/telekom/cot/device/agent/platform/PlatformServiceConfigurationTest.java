package com.telekom.cot.device.agent.platform;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.telekom.cot.device.agent.common.util.ValidationUtil;

public class PlatformServiceConfigurationTest {
    //test PlatformServiceConfiguration

	private static final String HOSTNAME = "host";
	private static final String EXTERNAL_ID_TYPE = "externalIdType";
	private static final String EXTERNAL_ID_VALUE = "externalIdValue";
	
    private PlatformServiceConfiguration.ExternalIdConfig externalIdConfig;
    private PlatformServiceConfiguration platformServiceConfig;
    
    @Before
    public void setUp() {
        externalIdConfig = new PlatformServiceConfiguration.ExternalIdConfig();
        externalIdConfig.setType(EXTERNAL_ID_TYPE);
        externalIdConfig.setValue(EXTERNAL_ID_VALUE);

        platformServiceConfig = new PlatformServiceConfiguration() {};
        platformServiceConfig.setHostName(HOSTNAME);
        platformServiceConfig.setExternalIdConfig(externalIdConfig);
    }
	
	@Test
	public void testSettersAndGetters() {
		assertEquals(HOSTNAME, platformServiceConfig.getHostName());
		assertEquals(externalIdConfig, platformServiceConfig.getExternalIdConfig());
		assertEquals(EXTERNAL_ID_TYPE, platformServiceConfig.getExternalIdConfig().getType());
		assertEquals(EXTERNAL_ID_VALUE, platformServiceConfig.getExternalIdConfig().getValue());
	}
	
	@Test
	public void testExternalId() {
		//test enum
		assertEquals("HARDWARE_SERIAL", PlatformServiceConfiguration.ExternalIdConfig.ValueTemplates.HARDWARE_SERIAL.name());
		assertEquals("NO_TEMPLATE", PlatformServiceConfiguration.ExternalIdConfig.ValueTemplates.NO_TEMPLATE.name());
		assertEquals("TYPE_HARDWARE_SERIAL", PlatformServiceConfiguration.ExternalIdConfig.ValueTemplates.TYPE_HARDWARE_SERIAL.name());

		//test valueTemplate getters and setters
		PlatformServiceConfiguration.ExternalIdConfig externalIdConfig = new PlatformServiceConfiguration.ExternalIdConfig();
		externalIdConfig.setValueTemplate(PlatformServiceConfiguration.ExternalIdConfig.ValueTemplates.HARDWARE_SERIAL);
		assertEquals(PlatformServiceConfiguration.ExternalIdConfig.ValueTemplates.HARDWARE_SERIAL, externalIdConfig.getValueTemplate());
	}

	@Test
	public void testBeanValidation() {
        // prepared configuration (@Before) must be valid
        assertTrue(ValidationUtil.isValid(platformServiceConfig));

        // no externalIdConfig
        platformServiceConfig.setExternalIdConfig(null);
        assertFalse(ValidationUtil.isValid(platformServiceConfig));
	    
        // no externalIdConfig type
        platformServiceConfig.setExternalIdConfig(externalIdConfig);
        externalIdConfig.setType(null);
        assertFalse(ValidationUtil.isValid(platformServiceConfig));

        // no externalIdConfig value
        platformServiceConfig.setExternalIdConfig(externalIdConfig);
        externalIdConfig.setType(EXTERNAL_ID_TYPE);
        externalIdConfig.setValue(null);
        assertTrue(ValidationUtil.isValid(platformServiceConfig));
	}
}
