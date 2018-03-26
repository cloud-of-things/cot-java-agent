package com.telekom.cot.device.agent.platform;

import static org.junit.Assert.*;

import com.telekom.cot.device.agent.common.util.ValidationUtil;
import org.junit.Assert;
import org.junit.Test;

public class PlatformServiceConfigurationTest {

	private static final String PROXY_HOST = "proxyHost";
	private static final String PROXY_PORT = "12345";
	private static final String HOSTNAME = "host";
	private static final String EXTERNAL_ID_TYPE = "externalIdType";
	private static final String EXTERNAL_ID_VALUE = "externalIdValue";
	
	@Test
	public void testSettersAndGetters() {
		PlatformServiceConfiguration.ExternalIdConfig externalIdConfig = new PlatformServiceConfiguration.ExternalIdConfig();
		externalIdConfig.setType(EXTERNAL_ID_TYPE);
		externalIdConfig.setValue(EXTERNAL_ID_VALUE);
		
		PlatformServiceConfiguration platformServiceConfig = new PlatformServiceConfiguration();
		platformServiceConfig.setHostName(HOSTNAME);
		platformServiceConfig.setProxyHost(PROXY_HOST);
		platformServiceConfig.setProxyPort(PROXY_PORT);
		platformServiceConfig.setExternalIdConfig(externalIdConfig);

		assertEquals(HOSTNAME, platformServiceConfig.getHostName());
		assertEquals(PROXY_HOST, platformServiceConfig.getProxyHost());
		assertEquals(PROXY_PORT, platformServiceConfig.getProxyPort());
		assertEquals(externalIdConfig, platformServiceConfig.getExternalIdConfig());
		assertEquals(EXTERNAL_ID_TYPE, platformServiceConfig.getExternalIdConfig().getType());
		assertEquals(EXTERNAL_ID_VALUE, platformServiceConfig.getExternalIdConfig().getValue());
	}
	
	@Test
	public void testToString() {
		String expected = PlatformServiceConfiguration.class.getSimpleName() + " [hostName=" + HOSTNAME + ", proxyHost="
				+ PROXY_HOST + ", proxyPort=" + PROXY_PORT
				+ ", externalId=" + PlatformServiceConfiguration.ExternalIdConfig.class.getSimpleName() 
				+ " [type=" + EXTERNAL_ID_TYPE + ", value=" + EXTERNAL_ID_VALUE + "]]";
		
		PlatformServiceConfiguration.ExternalIdConfig externalIdConfig = new PlatformServiceConfiguration.ExternalIdConfig();
		externalIdConfig.setType(EXTERNAL_ID_TYPE);
		externalIdConfig.setValue(EXTERNAL_ID_VALUE);
		
		PlatformServiceConfiguration platformServiceConfig = new PlatformServiceConfiguration();
		platformServiceConfig.setHostName(HOSTNAME);
		platformServiceConfig.setProxyHost(PROXY_HOST);
		platformServiceConfig.setProxyPort(PROXY_PORT);
		platformServiceConfig.setExternalIdConfig(externalIdConfig);
		
		assertEquals(expected, platformServiceConfig.toString());
	}

	@Test
	public void testExternalId() {
		//test enum
		Assert.assertEquals("HARDWARE_SERIAL", PlatformServiceConfiguration.ExternalIdConfig.ValueTemplates.HARDWARE_SERIAL.name());
		Assert.assertEquals("NO_TEMPLATE", PlatformServiceConfiguration.ExternalIdConfig.ValueTemplates.NO_TEMPLATE.name());
		Assert.assertEquals("TYPE_HARDWARE_SERIAL", PlatformServiceConfiguration.ExternalIdConfig.ValueTemplates.TYPE_HARDWARE_SERIAL.name());

		//test valueTemplate getters and setters
		PlatformServiceConfiguration.ExternalIdConfig externalIdConfig = new PlatformServiceConfiguration.ExternalIdConfig();
		externalIdConfig.setValueTemplate(PlatformServiceConfiguration.ExternalIdConfig.ValueTemplates.HARDWARE_SERIAL);
		Assert.assertEquals(PlatformServiceConfiguration.ExternalIdConfig.ValueTemplates.HARDWARE_SERIAL, externalIdConfig.getValueTemplate());
	}

	@Test
	public void testBeanValidation() {
		PlatformServiceConfiguration.ExternalIdConfig externalIdConfig = new PlatformServiceConfiguration.ExternalIdConfig();
		PlatformServiceConfiguration platformServiceConfiguration = new PlatformServiceConfiguration();

		//both null
		Assert.assertFalse(ValidationUtil.isValid(platformServiceConfiguration));
		Assert.assertFalse(ValidationUtil.isValid(externalIdConfig));

		externalIdConfig.setType("Test");
		//externalId should be valid
		Assert.assertTrue(ValidationUtil.isValid(externalIdConfig));
		//platformService should still be invalid
		platformServiceConfiguration.setExternalIdConfig(externalIdConfig);
		Assert.assertFalse(ValidationUtil.isValid(platformServiceConfiguration));

		//OK
		platformServiceConfiguration.setHostName("test");
		Assert.assertTrue(ValidationUtil.isValid(platformServiceConfiguration));
	}
}
