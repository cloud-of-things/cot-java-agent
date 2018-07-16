package com.telekom.cot.device.agent.platform.rest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.telekom.cot.device.agent.common.util.ValidationUtil;
import com.telekom.cot.device.agent.platform.PlatformServiceConfiguration;

public class PlatformServiceRestConfigurationTest {

	private static final String PROXY_HOST = "proxyHost";
	private static final String PROXY_PORT = "12345";
	private static final Integer OPERATIONS_REQUEST_SIZE = 5;
	private static final String HOSTNAME = "host";
	private static final String EXTERNAL_ID_TYPE = "externalIdType";
	private static final String EXTERNAL_ID_VALUE = "externalIdValue";
	
	private PlatformServiceConfiguration.ExternalIdConfig externalIdConfig;
	private PlatformServiceRestConfiguration.RestConfiguration restConfiguration;
	private PlatformServiceRestConfiguration platformServiceRestConfig;
	
	@Before
	public void setUp() {
        externalIdConfig = new PlatformServiceConfiguration.ExternalIdConfig();
        externalIdConfig.setType(EXTERNAL_ID_TYPE);
        externalIdConfig.setValue(EXTERNAL_ID_VALUE);

        restConfiguration = new PlatformServiceRestConfiguration.RestConfiguration();
        restConfiguration.setProxyHost(PROXY_HOST);
        restConfiguration.setProxyPort(PROXY_PORT);
        restConfiguration.setOperationsRequestSize(OPERATIONS_REQUEST_SIZE);
        
        platformServiceRestConfig = new PlatformServiceRestConfiguration();
        platformServiceRestConfig.setHostName(HOSTNAME);
        platformServiceRestConfig.setExternalIdConfig(externalIdConfig);
        platformServiceRestConfig.setRestConfiguration(restConfiguration);
	}
	
	@Test
	public void testSettersAndGetters() {
		assertEquals(HOSTNAME, platformServiceRestConfig.getHostName());
		assertEquals(externalIdConfig, platformServiceRestConfig.getExternalIdConfig());
		assertEquals(EXTERNAL_ID_TYPE, platformServiceRestConfig.getExternalIdConfig().getType());
		assertEquals(EXTERNAL_ID_VALUE, platformServiceRestConfig.getExternalIdConfig().getValue());
		assertEquals(restConfiguration, platformServiceRestConfig.getRestConfiguration());
        assertEquals(PROXY_HOST, platformServiceRestConfig.getRestConfiguration().getProxyHost());
        assertEquals(PROXY_PORT, platformServiceRestConfig.getRestConfiguration().getProxyPort());
        assertEquals(OPERATIONS_REQUEST_SIZE, platformServiceRestConfig.getRestConfiguration().getOperationsRequestSize());
	}
	
	@Test
	public void testToString() {
		String expected = PlatformServiceRestConfiguration.class.getSimpleName() + " [hostName=" + HOSTNAME 
				+ ", externalId=" + PlatformServiceConfiguration.ExternalIdConfig.class.getSimpleName() 
				+ " [type=" + EXTERNAL_ID_TYPE + ", value=" + EXTERNAL_ID_VALUE + "]"
				+ ", restConfiguration=" + PlatformServiceRestConfiguration.RestConfiguration.class.getSimpleName()
				+ " [proxyHost=" + PROXY_HOST + ", proxyPort=" + PROXY_PORT + ", operationsRequestSize=" + OPERATIONS_REQUEST_SIZE + "]"
				+ "]";
		
		assertEquals(expected, platformServiceRestConfig.toString());
	}

	@Test
	public void testBeanValidation() {
	    // prepared configuration (@Before) must be valid
        assertTrue(ValidationUtil.isValid(platformServiceRestConfig));

        // configuration with no proxy host and set proxy port must be valid
        restConfiguration.setProxyHost(null);
        restConfiguration.setProxyPort(PROXY_PORT);
        assertTrue(ValidationUtil.isValid(platformServiceRestConfig));
        
        // configuration with set proxy host and no proxy port must be valid
        restConfiguration.setProxyHost(PROXY_HOST);
        restConfiguration.setProxyPort(null);
        assertTrue(ValidationUtil.isValid(platformServiceRestConfig));

        // configuration with no proxy host and no proxy port must be valid
        restConfiguration.setProxyHost(null);
        restConfiguration.setProxyPort(null);
        assertTrue(ValidationUtil.isValid(platformServiceRestConfig));

        // configuration with no rest configuration is invalid
        platformServiceRestConfig.setRestConfiguration(null);
        assertFalse(ValidationUtil.isValid(platformServiceRestConfig));
	}
}
