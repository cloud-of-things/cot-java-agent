package com.telekom.cot.device.agent.service.configuration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.telekom.cot.device.agent.common.util.InjectionUtil;
import com.telekom.cot.device.agent.service.configuration.testconfigs.ConfigurationEmptyAnnotation;
import com.telekom.cot.device.agent.service.configuration.testconfigs.ConfigurationNoAnnotation;
import com.telekom.cot.device.agent.service.configuration.testconfigs.PlatformConfiguration;

public class YamlFileRealTest {

	private static final String NOT_EXISTING_FILE = "src/test/resources/notExisting.yaml";
	private static final String READ_TEST_YAML_FILE = "src/test/resources/readtest.yaml";
	private static final String WRITE_TEST_YAML_FILE = "src/test/resources/writetest.yaml";
	
	@Mock private Logger mockLogger;
	
	private YamlFile readTestYamlFile = YamlFile.open(READ_TEST_YAML_FILE, false);
	private YamlFile writeTestYamlFile = YamlFile.open(WRITE_TEST_YAML_FILE, true);

	@Before
	public void setUp() {
		// init and inject mocks
		MockitoAnnotations.initMocks(this);
		InjectionUtil.injectStatic(YamlFile.class, mockLogger);
	}
	
	@After
	public void tearDown() {
		try {
			Files.deleteIfExists(Paths.get(NOT_EXISTING_FILE));
			Files.deleteIfExists(Paths.get(WRITE_TEST_YAML_FILE));
		} catch (IOException e) {
		}
	}
	
	
	/*
	 * -------------------------------------------------------------------------------------------------------------------------
	 * test open
	 * -------------------------------------------------------------------------------------------------------------------------
	 */
	
	/**
	 * test method open, file doesn't exist, don't create it
	 */
	@Test
	public void testOpenNotExistingFileNoCreate() {
		assertNull(YamlFile.open(NOT_EXISTING_FILE, false));
		verify(mockLogger).error(eq("can't read YAML file '{}'"), eq(NOT_EXISTING_FILE), any(IOException.class));
	}
	
	/**
	 * test method open, file doesn't exist, can't create it
	 */
	@Test
	public void testOpenNotExistingFileCantCreate() {
		assertNull(YamlFile.open("src/test/notExistingDir/notExisting.yaml", true));
		verify(mockLogger).error(eq("can't create yaml file '{}'"), eq("src/test/notExistingDir/notExisting.yaml"), any(IOException.class));
	}
	
	/**
	 * test method open, file doesn't exist, create it
	 */
	@Test
	public void testOpenNotExistingFileCreate() {
		assertNotNull(YamlFile.open(NOT_EXISTING_FILE, true));
		assertTrue(Files.exists(Paths.get(NOT_EXISTING_FILE)));
		verify(mockLogger).info("opened (created) yaml file '{}' successfully", NOT_EXISTING_FILE);
	}
	
	/**
	 * test method open
	 */
	@Test
	public void testOpen() {
		assertNotNull(YamlFile.open(READ_TEST_YAML_FILE, false));
		verify(mockLogger).info("opened yaml file '{}' successfully", READ_TEST_YAML_FILE);
	}

	/**
	 * test method openInMemory positive test
	 */
	@Test
	public void testOpenInMemory(){
		String agentYamlContent = "test: test";
		assertNotNull(YamlFile.openInMemory(agentYamlContent));
		verify(mockLogger).info("opened yaml file from content successfully");
	}

	/**
	 * test method openInMemory with empty agentYamlFile
	 */
	@Test
	public void testOpenInMemoryEmpty(){
		String agentYamlContent = "";
		assertNull(YamlFile.openInMemory(agentYamlContent));
		verify(mockLogger).error("no content given to open yaml file");
	}

	/**
	 * test method openInMemory with bad content of agentYamlFile
	 */
	@Test
	public void testOpenInMemoryBadContent(){
		String agentYamlContent = "test ";
		assertNull(YamlFile.openInMemory(agentYamlContent));
		verify(mockLogger).error("can't open yaml file from content");
	}

    /**
     * test method openInMemory, yamlMapper.readTree throws exception
     */
    @Test
    public void testOpenInMemoryReadTreeException() throws Exception {
        assertNull(YamlFile.openInMemory("testContent"));
        verify(mockLogger).error(eq("can't read YAML file from content"), any(Exception.class));
    }
    
    /**
     * test method openInMemory, yamlMapper.readTree returns null
     */
    @Test
    public void testOpenInMemoryNoRootNode() throws Exception {
        assertNotNull(YamlFile.openInMemory(" "));
        verify(mockLogger).debug("content is empty, create new configuration root node");
    }
    
	/**
	 * -------------------------------------------------------------------------------------------------------------------------
	 * test filePath
	 * -------------------------------------------------------------------------------------------------------------------------
	 */

	@Test
	public void testGetFilePath(){
		YamlFile testFile = YamlFile.open(READ_TEST_YAML_FILE, false);
		assertEquals(READ_TEST_YAML_FILE, testFile.getFilePath());
	}

	/*
	 * -------------------------------------------------------------------------------------------------------------------------
	 * test readConfiguration
	 * -------------------------------------------------------------------------------------------------------------------------
	 */
	
	/**
	 * test method readConfiguration, configuration has no annotation
	 */
	@Test
	public void testReadConfigurationNoAnnotation(){
		ConfigurationNoAnnotation configuration = readTestYamlFile.getConfiguration(ConfigurationNoAnnotation.class);

		assertNotNull(configuration);
		assertEquals("hallo", configuration.getAgent().getPlatform().getTextProperty());
		assertEquals(1, configuration.getAgent().getPlatform().getIntProperty());
		assertEquals(1.23, configuration.getAgent().getPlatform().getDoubleProperty(), 0.00001);
		assertEquals(false, configuration.getAgent().getPlatform().isBooleanProperty());
		assertEquals("value11", configuration.getAgent().getTestService1().getProperty1());
		assertEquals(12, configuration.getAgent().getTestService1().getProperty2());
		assertEquals("value21", configuration.getAgent().getTestService2().getProperty1());
		assertEquals(22, configuration.getAgent().getTestService2().getProperty2());
	}
	
	/**
	 * test method readConfiguration, configuration path (at annotation) doesn't exist in yaml file
	 */
	@Test
	public void testReadConfigurationNotExistingConfigurationPath(){
		assertNull(readTestYamlFile.getConfiguration(ConfigurationNotExistingPath.class));
	}

	/**
	 * test method readConfiguration, configuration has valid path (at annotation) but isn't readable
	 */
	@Test
	public void testReadConfigurationNotReadable(){
		assertNull(readTestYamlFile.getConfiguration(ConfigurationNotReadable.class));
		verify(mockLogger).error(eq("can't create configuration of type '{}'"), eq(ConfigurationNotReadable.class), any(JsonProcessingException.class));
	}

	/**
	 * test method readConfiguration, configuration path (at annotation) is empty
	 */
	@Test
	public void testReadConfigurationAnnotationEmpty(){
		ConfigurationEmptyAnnotation configuration = readTestYamlFile.getConfiguration(ConfigurationEmptyAnnotation.class);

		assertNotNull(configuration);
		assertEquals("hallo", configuration.getAgent().getPlatform().getTextProperty());
		assertEquals(1, configuration.getAgent().getPlatform().getIntProperty());
		assertEquals(1.23, configuration.getAgent().getPlatform().getDoubleProperty(), 0.00001);
		assertEquals(false, configuration.getAgent().getPlatform().isBooleanProperty());
		assertEquals("value11", configuration.getAgent().getTestService1().getProperty1());
		assertEquals(12, configuration.getAgent().getTestService1().getProperty2());
		assertEquals("value21", configuration.getAgent().getTestService2().getProperty1());
		assertEquals(22, configuration.getAgent().getTestService2().getProperty2());
	}

	/**
	 * test method readConfiguration (requested configuration is part of yaml file)
	 */
	@Test
	public void testReadConfiguration(){
		PlatformConfiguration configuration = readTestYamlFile.getConfiguration(PlatformConfiguration.class);

		assertNotNull(configuration);
		assertEquals("hallo", configuration.getTextProperty());
		assertEquals(1, configuration.getIntProperty());
		assertEquals(1.23, configuration.getDoubleProperty(), 0.00001);
		assertEquals(false, configuration.isBooleanProperty());
	}
	
	/*
	 * -------------------------------------------------------------------------------------------------------------------------
	 * test writeConfiguration
	 * -------------------------------------------------------------------------------------------------------------------------
	 */

	/**
	 * test method writeConfiguration, configuration has no annotation
	 */
	@Test
	public void testWriteConfigurationNoAnnotation() {
		ConfigurationNoAnnotation configuration = new ConfigurationNoAnnotation();
		configuration.getAgent().getPlatform().setTextProperty("writeTest");
		configuration.getAgent().getPlatform().setIntProperty(23);
		configuration.getAgent().getTestService2().setProperty1("testProperty");
		
		assertTrue(writeTestYamlFile.putConfiguration(configuration, true));
		
		ConfigurationNoAnnotation readConfiguration = writeTestYamlFile.getConfiguration(ConfigurationNoAnnotation.class);
		assertNotNull(readConfiguration);
		assertEquals("writeTest", readConfiguration.getAgent().getPlatform().getTextProperty());
		assertEquals(23, readConfiguration.getAgent().getPlatform().getIntProperty());
		assertEquals("testProperty", readConfiguration.getAgent().getTestService2().getProperty1());
	}

	/*
	 * -------------------------------------------------------------------------------------------------------------------------
	 * test getFileContentAsString
	 * -------------------------------------------------------------------------------------------------------------------------
	 */
	/**
	 * test method getFileContentAsString
	 */
	@Test
	public void testGetFileContentAsString() {
		assertNotNull(readTestYamlFile.getContent());
	}
	
	/*
	 * -------------------------------------------------------------------------------------------------------------------------
	 * test-configurations
	 * -------------------------------------------------------------------------------------------------------------------------
	 */
	
	@ConfigurationPath("does.not.exist")
	public class ConfigurationNotExistingPath implements Configuration {
	}

	@ConfigurationPath("agent.platform")
	public class ConfigurationNotReadable implements Configuration {
	}
}

