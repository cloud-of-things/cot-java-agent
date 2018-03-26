package com.telekom.cot.device.agent.service.configuration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.telekom.cot.device.agent.common.util.InjectionUtil;
import com.telekom.cot.device.agent.service.configuration.testconfigs.ConfigurationEmptyAnnotation;
import com.telekom.cot.device.agent.service.configuration.testconfigs.ConfigurationNoAnnotation;

public class YamlFileMockTest {

    private static final String TEST_CONFIGURATION_FILE = "src/test/resources/mocktest.yaml";
    private static final String CONFIGURATION_CONTENT = "testConfigurationContent";
	
	@Mock private Logger mockLogger;
	@Mock private YAMLMapper mockYamlMapper;
	@Mock private ObjectNode mockRootNode;
	@Mock private ObjectNode mockChildNode1;
	@Mock private ObjectNode mockChildNode2;
	@Mock private ObjectNode mockChildNode3;
	@Mock private ObjectNode mockConfigurationTree;

	private ConfigurationNoAnnotation configurationNoAnnotation = new ConfigurationNoAnnotation();
	private ConfigurationEmptyAnnotation configurationEmptyAnnotation = new ConfigurationEmptyAnnotation();
	private ConfigurationAgentTest configurationAgentTest = new ConfigurationAgentTest();
	
	private YamlFile yamlFile;
	
	@Before
	public void setUp() throws Exception {
		yamlFile = YamlFile.open(TEST_CONFIGURATION_FILE, false);
		
		// initialize and inject mocks
		MockitoAnnotations.initMocks(this);
		InjectionUtil.injectStatic(YamlFile.class, mockLogger);
		InjectionUtil.inject(yamlFile, mockYamlMapper);
		InjectionUtil.inject(yamlFile, mockRootNode);
		
		// behavior of mocked yaml mapper
		when(mockYamlMapper.treeToValue(mockRootNode, ConfigurationNoAnnotation.class)).thenReturn(configurationNoAnnotation);
		when(mockYamlMapper.treeToValue(mockRootNode, ConfigurationEmptyAnnotation.class)).thenReturn(configurationEmptyAnnotation);
		when(mockYamlMapper.treeToValue(mockChildNode2, ConfigurationAgentTest.class)).thenReturn(configurationAgentTest);
		when(mockYamlMapper.valueToTree(configurationNoAnnotation)).thenReturn(mockConfigurationTree);
		doNothing().when(mockYamlMapper).writeValue(any(FileOutputStream.class), any(JsonNode.class));
		doReturn(CONFIGURATION_CONTENT).when(mockYamlMapper).writeValueAsString(mockRootNode);
		
		// behavior of mocked root node
		when(mockRootNode.get("agent")).thenReturn(mockChildNode1);
		doReturn(null).when(mockRootNode).setAll(mockConfigurationTree);

		// behavior of mocked child node 1
		when(mockChildNode1.get("test")).thenReturn(mockChildNode2);

		// behavior of mocked child node 2
		when(mockChildNode2.get("missingNode")).thenReturn(null);
		when(mockChildNode2.putObject("missingNode")).thenReturn(mockChildNode3);

		// behavior of mocked child node 3
		doReturn(null).when(mockChildNode3).setAll(mockConfigurationTree);
	}
	
	/*
	 * -------------------------------------------------------------------------------------------------------------------------
	 * test open
	 * -------------------------------------------------------------------------------------------------------------------------
	 */
	
	/**
	 * test method open, file path is null or empty
	 */
	@Test
	public void testOpenNoFilepath() {
		assertNull(YamlFile.open(null, false));
		assertNull(YamlFile.open("", false));
		verify(mockLogger, times(2)).error("no file path given");
	}
	
    /**
     * test method openInMemory, content is null or empty
     */
    @Test
    public void testOpenInMemoryNoContent() {
        assertNull(YamlFile.openInMemory(null));
        assertNull(YamlFile.openInMemory(""));
        verify(mockLogger, times(2)).error("no content given to open yaml file");
    }
    
	/*
	 * -------------------------------------------------------------------------------------------------------------------------
	 * test getConfiguration
	 * -------------------------------------------------------------------------------------------------------------------------
	 */
	
	/**
	 * test method getConfiguration, no configuration type (null)
	 */
	@Test
	public void testGetConfigurationNoConfigurationType(){
		assertNull(yamlFile.getConfiguration(null));
		verify(mockLogger).error("no configuration type given");
	}

	/**
	 * test method getConfiguration, configuration has no annotation
	 */
	@Test
	public void testGetConfigurationNoAnnotation(){
		assertEquals(configurationNoAnnotation, yamlFile.getConfiguration(ConfigurationNoAnnotation.class));
		verify(mockLogger).warn("can't find annotation '@ConfigurationPath' at class '{}'", ConfigurationNoAnnotation.class.getName());
	}

	/**
	 * test method getConfiguration, annotation is empty
	 */
	@Test
	public void testGetConfigurationEmptyAnnotation(){
		assertEquals(configurationEmptyAnnotation, yamlFile.getConfiguration(ConfigurationEmptyAnnotation.class));
		verify(mockLogger).warn("no configuration path given at '@ConfigurationPath' annotation");
	}

	/**
	 * test method getConfiguration, no child node for path token "test" (from "agent.test")
	 */
	@Test
	public void testGetConfigurationNoChildNode(){
		reset(mockChildNode1);
		when(mockChildNode1.get("test")).thenReturn(null);
		
		assertNull(yamlFile.getConfiguration(ConfigurationAgentTest.class));
		verify(mockRootNode).get("agent");
	}

	/**
	 * test method getConfiguration, ObjectMapper.treeToValue throws JsonParseException
	 */
	@Test
	public void testGetConfigurationObjectMapperException() throws Exception {
		JsonParseException e = new JsonParseException(null, "test");
		doThrow(e).when(mockYamlMapper).treeToValue(mockChildNode2, ConfigurationAgentTest.class);
		
		assertNull(yamlFile.getConfiguration(ConfigurationAgentTest.class));
		verify(mockRootNode).get("agent");
		verify(mockChildNode1).get("test");
		verify(mockLogger).error("can't create configuration of type '{}'", ConfigurationAgentTest.class, e);
	}

	/*
	 * -------------------------------------------------------------------------------------------------------------------------
	 * test putConfiguration
	 * -------------------------------------------------------------------------------------------------------------------------
	 */

	/**
	 * test method putConfiguration, no configuration (null)
	 */
	@Test
	public void testPutConfigurationNoConfiguration() {
		assertFalse(yamlFile.putConfiguration(null, true));
	}

	/**
	 * test method putConfiguration, configuration has no annotation
	 */
	@Test
	public void testPutConfigurationNoAnnotation() {
		assertTrue(yamlFile.putConfiguration(configurationNoAnnotation, true));
		verify(mockLogger).warn("can't find annotation '@ConfigurationPath' at class '{}'", ConfigurationNoAnnotation.class.getName());
	}

	/**
	 * test method putConfiguration, annotation is empty
	 */
	@Test
	public void testPutConfigurationEmptyAnnotation() {
		assertTrue(yamlFile.putConfiguration(new ConfigurationEmptyAnnotation(), true));
		verify(mockLogger).warn("no configuration path given at '@ConfigurationPath' annotation");
	}

	/**
	 * test method putConfiguration, node (at configuration path) not exists
	 */
	@Test
	public void testPutConfigurationMissingNode() {
		assertTrue(yamlFile.putConfiguration(new ConfigurationMissingNode(), true));
	}	
	
	/**
	 * test method putConfiguration, no child node for path token "test" (from "agent.test")
	 */
	@Test
	public void testPutConfigurationNoChildNode() {
		InjectionUtil.inject(yamlFile, "rootNode", null);
		
		assertFalse(yamlFile.putConfiguration(configurationNoAnnotation, true));
		verify(mockLogger).error("can't get node to write configuration");
	}	
	
	/**
	 * test method putConfiguration, YAMLMapper.writeValue throws exception
	 */
	@Test
	public void testPutConfigurationValueToTreeException() throws Exception {
		IllegalArgumentException e = new IllegalArgumentException();
		doThrow(e).when(mockYamlMapper).valueToTree(configurationAgentTest);
		
		assertFalse(yamlFile.putConfiguration(configurationAgentTest, true));
		verify(mockLogger).error("can't set configuration at given node, reason: {}", e);
	}

	/**
	 * test method putConfiguration, YAMLMapper.writeValue throws exception
	 */
	@Test
	public void testPutConfigurationWriteValueException() throws Exception {
		IOException e = new IOException();
		doThrow(e).when(mockYamlMapper).writeValue(any(FileOutputStream.class), eq(mockRootNode));
		
		assertFalse(yamlFile.putConfiguration(configurationAgentTest, true));
		verify(mockLogger).error("can't save YAML file at '{}'", TEST_CONFIGURATION_FILE, e);
	}

    /**
     * test method putConfiguration, save file
     */
    @Test
    public void testPutConfigurationSaveFile() throws Exception {
        assertTrue(yamlFile.putConfiguration(configurationAgentTest, true));
        verify(mockYamlMapper).writeValue(any(FileOutputStream.class), any(JsonNode.class));
    }
	
    /**
     * test method putConfiguration, don't save file
     */
    @Test
    public void testPutConfigurationDontSaveFile() throws Exception {
        assertTrue(yamlFile.putConfiguration(configurationAgentTest, false));
        verify(mockYamlMapper, never()).writeValue(any(FileOutputStream.class), any(JsonNode.class));
    }
    
    /*
     * -------------------------------------------------------------------------------------------------------------------------
     * test getContent
     * -------------------------------------------------------------------------------------------------------------------------
     */

    /**
     * test method getContent(), no root node
     */
    @Test
    public void testGetContentNoRootNode() {
        InjectionUtil.inject(yamlFile, "rootNode", null);

        assertNull(yamlFile.getContent());
    }

    /**
     * test method getContent(), yamlMapper.writeValueAsString throws exception
     */
    @Test
    public void testGetContentYamlMapperException() throws Exception {
        JsonProcessingException exception = new JsonParseException(null, "error");
        doThrow(exception).when(mockYamlMapper).writeValueAsString(mockRootNode);
        
        assertNull(yamlFile.getContent());
        verify(mockLogger).error("can't get file content as string", exception);
    }

    /**
     * test method getContent()
     */
    @Test
    public void testGetContent() throws Exception {
        assertEquals(CONFIGURATION_CONTENT, yamlFile.getContent());
    }
    
	/*
	 * -------------------------------------------------------------------------------------------------------------------------
	 * test-configurations
	 * -------------------------------------------------------------------------------------------------------------------------
	 */

	@ConfigurationPath("agent.test")
	public class ConfigurationAgentTest implements Configuration {
	}

	@ConfigurationPath("agent.test.missingNode")
	public class ConfigurationMissingNode implements Configuration {
	}
}
