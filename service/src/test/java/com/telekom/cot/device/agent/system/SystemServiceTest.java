package com.telekom.cot.device.agent.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.ConfigurationNotFoundException;
import com.telekom.cot.device.agent.common.exc.PropertyNotFoundException;
import com.telekom.cot.device.agent.common.util.InjectionUtil;
import com.telekom.cot.device.agent.service.AgentServiceProvider;
import com.telekom.cot.device.agent.service.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.service.configuration.MobilePropertiesConfiguration;
import com.telekom.cot.device.agent.system.properties.ConfigurationProperties;
import com.telekom.cot.device.agent.system.properties.MobileProperties;

public class SystemServiceTest {

	private static final String IMEI = "testIMEI";
	private static final String CELLID = "testCellId";
	private static final String ICCID = "testICCID";

	private static final String CONFIGURATION = "testConfiguration";

	@Mock
	private Logger mockLogger;
	@Mock
	private ConfigurationManager mockConfigurationManager;
	@Mock
	private AgentServiceProvider mockServiceProvider;
	@Mock
	private SystemService mockSystemService;

	private MobilePropertiesConfiguration mobilePropertiesConfiguration = new MobilePropertiesConfiguration(IMEI,
			CELLID, ICCID);

	private AbstractSystemService systemService = new AbstractSystemService() {
	};

	@Before
	public void setUp() throws Exception {
		// initialize and inject mocks
		MockitoAnnotations.initMocks(this);
		InjectionUtil.injectStatic(AbstractSystemService.class, mockLogger);
		InjectionUtil.inject(systemService, mockConfigurationManager);
		InjectionUtil.inject(systemService, mockServiceProvider);

		// behavior of mocked ConfigurationManager
		when(mockConfigurationManager.getConfiguration()).thenReturn(CONFIGURATION);
		when(mockConfigurationManager.getConfiguration(MobilePropertiesConfiguration.class))
				.thenReturn(mobilePropertiesConfiguration);
		
		// behavior of mocked ServiceProvider
		when(mockServiceProvider.getService(SystemService.class)).thenReturn(mockSystemService);
	}

	/**
	 * test method start, no configuration manager is set
	 */
	@Test
	public void testStartNoConfigurationManager() throws Exception {
		InjectionUtil.inject(systemService, "configurationManager", null);

		systemService.start();

		// check empty ConfigurationProperties
		assertNotNull(systemService.getProperties(ConfigurationProperties.class));
		assertNull(systemService.getProperties(ConfigurationProperties.class).getConfig());

		// check empty MobileProperties
		assertNotNull(systemService.getProperties(MobileProperties.class));
		assertNull(systemService.getProperties(MobileProperties.class).getImei());
		assertNull(systemService.getProperties(MobileProperties.class).getCellId());
		assertNull(systemService.getProperties(MobileProperties.class).getIccid());

		// check logging
		verify(mockLogger).info(eq("can't get configuration content, create new empty configuration properties"),
				any(AbstractAgentException.class));
		verify(mockLogger).info(
				eq("can't get mobile properties from configuration, create new empty mobile properties"),
				any(AbstractAgentException.class));
	}

	/**
	 * test method start, ConfigurationManager.getConfiguration() throws exception
	 */
	@Test
	public void testStartGetConfigurationException() throws Exception {
		doThrow(new ConfigurationNotFoundException("not found")).when(mockConfigurationManager).getConfiguration();

		systemService.start();

		// check empty ConfigurationProperties
		assertNotNull(systemService.getProperties(ConfigurationProperties.class));
		assertNull(systemService.getProperties(ConfigurationProperties.class).getConfig());

		// check MobileProperties
		assertNotNull(systemService.getProperties(MobileProperties.class));
		assertEquals(IMEI, systemService.getProperties(MobileProperties.class).getImei());
		assertEquals(CELLID, systemService.getProperties(MobileProperties.class).getCellId());
		assertEquals(ICCID, systemService.getProperties(MobileProperties.class).getIccid());

		// check logging
		verify(mockLogger).info(eq("can't get configuration content, create new empty configuration properties"),
				any(AbstractAgentException.class));
		verify(mockLogger, never()).info(
				eq("can't get mobile properties from configuration, create new empty mobile properties"),
				any(AbstractAgentException.class));
	}

	/**
	 * test method start,
	 * ConfigurationManager.getConfiguration(MobilePropertiesConfiguration.class)
	 * throws exception
	 */
	@Test
	public void testStartGetMobilePropertiesException() throws Exception {
		doThrow(new ConfigurationNotFoundException("not found")).when(mockConfigurationManager)
				.getConfiguration(MobilePropertiesConfiguration.class);

		systemService.start();

		// check ConfigurationProperties
		assertNotNull(systemService.getProperties(ConfigurationProperties.class));
		assertEquals(CONFIGURATION, systemService.getProperties(ConfigurationProperties.class).getConfig());

		// check empty MobileProperties
		assertNotNull(systemService.getProperties(MobileProperties.class));
		assertNull(systemService.getProperties(MobileProperties.class).getImei());
		assertNull(systemService.getProperties(MobileProperties.class).getCellId());
		assertNull(systemService.getProperties(MobileProperties.class).getIccid());

		// check logging
		verify(mockLogger, never()).info(
				eq("can't get configuration content, create new empty configuration properties"),
				any(AbstractAgentException.class));
		verify(mockLogger).info(
				eq("can't get mobile properties from configuration, create new empty mobile properties"),
				any(AbstractAgentException.class));
	}

	/**
	 * test getProperties, property type is null
	 */
	@Test(expected = PropertyNotFoundException.class)
	public void testGetPropertiesNoType() throws Exception {
		systemService.getProperties(null);
	}

	/**
	 * test getProperties, properties of given type not available
	 */
	@Test(expected = PropertyNotFoundException.class)
	public void testGetPropertiesNoPropertiesOfGivenType() throws Exception {
		systemService.getProperties(ConfigurationProperties.class);
	}

	/**
	 * test getProperties
	 */
	@Test
	public void testGetProperties() throws Exception {
		systemService.start();
		ConfigurationProperties configurationProperties = systemService.getProperties(ConfigurationProperties.class);

		// check ConfigurationProperties
		assertNotNull(configurationProperties);
		assertEquals(CONFIGURATION, configurationProperties.getConfig());
	}
}
