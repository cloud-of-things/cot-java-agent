package com.telekom.cot.device.agent.service.configuration;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

class YamlFile {

	private static final Logger LOGGER = LoggerFactory.getLogger(YamlFile.class); 
	
	private String filePath;
	private YAMLMapper yamlMapper;
	private ObjectNode rootNode;

	/**
	 * hidden constructor
	 * @param filePath path of the YAML file to read
	 * @param readOnly tells whether file is read only
	 */
	private YamlFile(String filePath) {
		this.filePath = filePath;
		this.yamlMapper = new YAMLMapper();
	}

	/**
	 * open and read / create a new file and return a new instance of {@link YamlFile}
	 * @param filePath path of the YAML file to read
	 * @return a new instance of {@link YamlFile} or {@code null} if file can't be opened
	 */
	static YamlFile open(String filePath, boolean createIfNotExists) {
		// check file path
		if(StringUtils.isEmpty(filePath)) {
			LOGGER.error("no file path given");
			return null;
		}
		
		// create a new instance and try to read yaml file
		YamlFile yamlFile = new YamlFile(filePath);
		if(yamlFile.readFile()) {
			LOGGER.info("opened yaml file '{}' successfully", filePath);
			return yamlFile;
		}

		// create a new file?
		if (!createIfNotExists) {
			LOGGER.error("can't open yaml file '{}'", filePath);
			return null;
		}
		
		// create new file
		if (!yamlFile.createFile()) {
			LOGGER.error("can't open yaml file '{}'", filePath);
			return null;
		}
		
		LOGGER.info("opened (created) yaml file '{}' successfully", filePath);
		return yamlFile;
	}

	/**
	 * get a new instance of {@link YamlFile} as in memory file with given content
	 * @param filePath path of the configuration file to read
	 * @return a new instance of {@link YamlFile} or {@code null} if file can't be opened with given content
	 */
	static YamlFile openInMemory(String content) {
		// check content
		if(StringUtils.isEmpty(content)) {
			LOGGER.error("no content given to open yaml file");
			return null;
		}
		
		// create a new instance (no file path) and try to read from given content
		YamlFile yamlFile = new YamlFile(null);
		if(yamlFile.readFile(content)) {
			LOGGER.info("opened yaml file from content successfully");
			return yamlFile;
		}
		
		LOGGER.error("can't open yaml file from content");
		return null;
	}

	/**
	 * get current set file path
	 * @return current set file path
	 */
	String getFilePath() {
		return this.filePath;
	}
	
	/**
	 * save the current state of this YAML file at current set file path
	 * @return whether saving file has been successful
	 */
	boolean save() {
		return save(this.filePath);
	}
	
	/**
	 * save the current state of this YAML file at given file path
	 * @param filePath path of the file to save
	 * @return whether saving file has been successful
	 */
	boolean save(String filePath) {
		try {
			// write node tree (given by root node) to file (overwrite)
			yamlMapper.writeValue(new FileOutputStream(filePath), rootNode);
			this.filePath = filePath;
			return true;
		} catch (Exception e) {
			LOGGER.error("can't save YAML file at '{}'", filePath, e);
			return false;
		}
	}
	
	/**
	 * get a configuration of given type from the already opened YAML file;
	 * the configuration class to get can be annotated by '@ConfigurationPath'
	 * @param configurationType type of configuration to read (can be annotated by '@ConfigurationPath')
	 * @return the configuration read from YAML file or {@code null} if file can't be read
	 */
	<T extends Configuration> T getConfiguration(Class<T> configurationType) {
		// check type
		if (Objects.isNull(configurationType)) {
			LOGGER.error("no configuration type given");
			return null;
		}
		
		// get tokens of annotated configuration path (if exists)
		// and get child node by path tokens, don't create not existing nodes
		String[] pathTokens = getPathTokens(configurationType);
		ObjectNode childNode = getChildNodeByPathTokens(pathTokens, false);
		if (Objects.isNull(childNode)) {
			return null;
		}
		
		// read configuration
		return getConfigurationFromNode(childNode, configurationType);
	}

	/**
	 * put the given configuration into the YAML file;
	 * existing configuration properties out of scope of the given configuration object are kept unchanged,
	 * properties in scope of the configuration object are overwritten
	 * @param configuration configuration object to write
	 * @param saveFile whether to save the file ({@code true}) or to keep changes only in memory ({@code false}) 
	 * @return whether writing configuration to file has been successful
	 */
	boolean putConfiguration(Configuration configuration, boolean saveFile) {
		// check configuration
		if (Objects.isNull(configuration)) {
			LOGGER.error("no configuration given");
			return false;
		}
		
		// get tokens of annotated configuration path
		// and get child node by path tokens, create not existing nodes (by path token names)
		String[] pathTokens = getPathTokens(configuration.getClass());
		ObjectNode childNode = getChildNodeByPathTokens(pathTokens, true);
		if (Objects.isNull(childNode)) {
			LOGGER.error("can't get node to write configuration");
			return false;
		}
		
		// set configuration at child node
		if (!setConfigurationAtNode(configuration, childNode)) {
			return false;
		}
		
		// save file if requested
		return saveFile ? save() : true; 
	}
	
	/**
	 * get the content of the YAML file (represented by current root node) as string
	 * @return content of the YAML file or {@code null} if an error occurs
	 */
	String getContent() {
		try {
			return yamlMapper.writeValueAsString(rootNode);
		} catch (Exception e) {
			LOGGER.error("can't get file content as string", e);
			return null;
		}
	}
	
	/**
	 * read a YAML file at current set file path
	 */
	private boolean readFile() {
		try {
			LOGGER.info("read YAML file '{}'", filePath);
			rootNode = (ObjectNode)yamlMapper.readTree(new FileInputStream(filePath));
			if (Objects.isNull(rootNode)) {
				LOGGER.debug("file '{}' is empty, create new configuration root node", filePath);
				rootNode = yamlMapper.createObjectNode();
			}
			return true;
		} catch (Exception e) {
			LOGGER.error("can't read YAML file '{}'", filePath, e);
			return false;
		}
	}
	
	/**
	 * read the given content string as YAML file
	 */
	private boolean readFile(String content) {
		try {
			LOGGER.info("read YAML file from content string");
			rootNode = (ObjectNode)yamlMapper.readTree(content);
			if (Objects.isNull(rootNode)) {
				LOGGER.debug("content is empty, create new configuration root node");
				rootNode = yamlMapper.createObjectNode();
			}
			return true;
		} catch (Exception e) {
			LOGGER.error("can't read YAML file from content", e);
			return false;
		}
	}
	
	/**
	 * create a new YAML file at current set file path
	 */
	private boolean createFile() {
		LOGGER.debug("create new file '{}'", filePath);

		// create a new file
		try {
			Files.createFile(Paths.get(filePath).toAbsolutePath());
		} catch (IOException e) {
			LOGGER.error("can't create yaml file '{}'", filePath, e);
			return false;
		}

		// create a new empty root node
		rootNode = yamlMapper.createObjectNode();
		return true;
	}
	
	/**
	 * tokenize the configuration path (from annotation @ConfigurationPath)
	 */
	private String[] getPathTokens(Class<? extends Configuration> type) {
		// look for 'ConfigurationPath' annotation
		ConfigurationPath configurationPath = type.getDeclaredAnnotation(ConfigurationPath.class);
		if (Objects.isNull(configurationPath)) {
			LOGGER.warn("can't find annotation '@ConfigurationPath' at class '{}'", type.getName());
			return new String[0];
		}
		
		// check path value
		if (configurationPath.value().isEmpty()) {
			LOGGER.warn("no configuration path given at '@ConfigurationPath' annotation");
			return new String[0];
		}
		
		// tokenize path
		StringTokenizer tokenizer = new StringTokenizer(configurationPath.value(), ".");
		List<String> pathTokens = new ArrayList<>();
		while (tokenizer.hasMoreElements()) {
			pathTokens.add(tokenizer.nextToken());
		}
		
		return pathTokens.toArray(new String[0]);
	}
	
	/**
	 * traverses a node tree, beginning at 'rootNode' by the names given at 'pathTokens';
	 * if 'createMissingNodes' is true, not existing child nodes are created
	 */
	private ObjectNode getChildNodeByPathTokens(String[] pathTokens, boolean createMissingNodes) {
		// start with root node
		ObjectNode parentNode = rootNode;
		ObjectNode childNode = rootNode;

		for (String pathToken : pathTokens) {
			// get child node by name 'pathToken' and check 
			Object childNodeObject = parentNode.get(pathToken);
			if(Objects.nonNull(childNodeObject) && ObjectNode.class.isInstance(childNodeObject)) {
			    childNode = ObjectNode.class.cast(childNodeObject);
			}
			else {
				// if child node not exists and should not be created then stop search
				if (!createMissingNodes) {
					return null;
				}
				// create and append child node
				childNode = parentNode.putObject(pathToken);
			}
			
			// parent node of next iteration = current child node
			parentNode = childNode;
		}
		
		return childNode;
	}
	
	/**
	 * creates a configuration object of given type from given object node 
	 */
	private <T extends Configuration> T getConfigurationFromNode(ObjectNode node, Class<T> type) {
		try {
			return yamlMapper.treeToValue(node, type);
		} catch (JsonProcessingException e) {
			LOGGER.error("can't create configuration of type '{}'", type, e);
			return null;
		}
	}
	
	/**
	 * creates a node tree from given configuration and sets the new tree as child of given node
	 */
	private boolean setConfigurationAtNode(Configuration configuration, ObjectNode node) {
		try {
			// get node tree from configuration object and append at 'node'
			ObjectNode configurationTree = yamlMapper.valueToTree(configuration);
			node.setAll(configurationTree);
			return true;
		} catch (Exception e) {
			LOGGER.error("can't set configuration at given node, reason: {}", e);
			return false;
		}
	}
}
