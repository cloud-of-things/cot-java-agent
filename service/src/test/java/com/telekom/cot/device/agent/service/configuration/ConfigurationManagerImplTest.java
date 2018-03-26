package com.telekom.cot.device.agent.service.configuration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.exc.ConfigurationNotFoundException;
import com.telekom.cot.device.agent.common.util.InjectionUtil;

public class ConfigurationManagerImplTest {

	private static final String YAML_FILE_CONTENT = "agent:\n  test:\n    property: value";
	
	@Mock private Logger mockLogger;
	@Mock private YamlFile mockYamlFile;
	
	private HashMap<Class<? extends Configuration>, Configuration> configurationMap;
	private TestConfiguration2 testConfiguration = new TestConfiguration2();
	
	private ConfigurationManager configurationManager;
	
	@Before
	public void setUp() {
		configurationManager = ConfigurationManagerImpl.getInstance(null);
		
		// init and inject mocks
		MockitoAnnotations.initMocks(this);
		InjectionUtil.injectStatic(ConfigurationManagerImpl.class, mockLogger);
		InjectionUtil.inject(configurationManager, mockYamlFile);
		
		// init and inject configuration map
		configurationMap = new HashMap<>();
		configurationMap.put(TestConfiguration1.class, new TestConfiguration1());
		InjectionUtil.inject(configurationManager, configurationMap);
		
		// behavior of mocked configuration yaml file
		when(mockYamlFile.getConfiguration(TestConfiguration2.class)).thenReturn(testConfiguration);
		when(mockYamlFile.getContent()).thenReturn(YAML_FILE_CONTENT);
		
		// behavior of mocked device credentials yaml file
//		when(mockDeviceCredentialsYamlFile.getConfiguration(AgentCredentials.class)).thenReturn(agentCredentials);
	}
	
	/**
	 * test method getConfiguration, no configuration type
	 */
	@Test(expected=ConfigurationNotFoundException.class)
	public void testGetConfigurationNoConfigurationType() throws Exception {
		configurationManager.getConfiguration(null);
	}
	
	/**
	 * test method getConfiguration, configuration is null
	 */
	@Test(expected=ConfigurationNotFoundException.class)
	public void testGetConfigurationNullConfiguration() throws Exception {
		configurationMap.put(TestConfiguration1.class, null);
		configurationManager.getConfiguration(TestConfiguration1.class);
	}
	
	/**
	 * test method getConfiguration, configuration exists
	 */
	@Test
	public void testGetConfigurationExistingConfiguration() throws Exception {
		assertNotNull(configurationManager.getConfiguration(TestConfiguration1.class));
	}
	
	/**
	 * test method getConfiguration, read configuration returns null
	 */
	@Test(expected=ConfigurationNotFoundException.class)
	public void testGetConfigurationReadConfigurationNull() throws Exception {
		doReturn(null).when(mockYamlFile).getConfiguration(TestConfiguration2.class);
		
		configurationManager.getConfiguration(TestConfiguration2.class);
	}
	
	/**
	 * test method getConfiguration, read configuration
	 */
	@Test
	public void testGetConfigurationReadConfiguration() throws Exception {
		assertSame(testConfiguration, configurationManager.getConfiguration(TestConfiguration2.class));
	}
	
	/**
	 * test method getConfiguration (as string), content is null
	 */
	@Test(expected=ConfigurationNotFoundException.class)
	public void testGetConfigurationAsStringNullContent() throws Exception {
		doReturn(null).when(mockYamlFile).getContent();

		configurationManager.getConfiguration();
	}
	
	/**
	 * test method getConfiguration (as string)
	 */
	@Test
	public void testGetConfigurationAsString() throws Exception {
		assertEquals(YAML_FILE_CONTENT, configurationManager.getConfiguration());
	}
	
	public class TestConfiguration1 implements Configuration {
	}

	public class TestConfiguration2 implements Configuration {
	}
}
