package com.telekom.cot.device.agent.common.configuration;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.assertIsTrue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.telekom.cot.device.agent.common.annotations.ConfigurationPath;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.YamlFileException;

class ConfigurationFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationFile.class);
    private Path filePath;
    private String content;
    private YAMLMapper yamlMapper;
    private ObjectNode rootNode;
    private final static Class<YamlFileException> excClass = YamlFileException.class;

    /**
     * hidden constructor
     * 
     * @param filePath
     *            path of the YAML file to read
     */
    private ConfigurationFile(Path filePath) {
        this.filePath = filePath;
        this.yamlMapper = new YAMLMapper();
    }

    /**
     * hidden constructor
     * 
     * @param content
     *            as YAML format
     */
    private ConfigurationFile(String content) {
        this.content = content;
        this.yamlMapper = new YAMLMapper();
    }

    /**
     * open, read and return a new instance of {@link ConfigurationFile}
     * 
     * @param filePath
     *            path of the YAML file to read
     * @return a new instance of {@link ConfigurationFile}
     * @throws AbstractAgentException
     *             if can't read the yaml file, this exception is thrown
     */
    static ConfigurationFile open(Path filePath) throws AbstractAgentException {
        // check file path
        assertIsTrue(Objects.nonNull(filePath), excClass, LOGGER, "no file path given");
        // try to read yaml file
        ConfigurationFile yamlFile = new ConfigurationFile(filePath);
        assertIsTrue(yamlFile.readFile(), excClass, LOGGER, "can't read file: " + filePath);
        LOGGER.info("opened yaml file '{}' successfully", filePath);
        return yamlFile;
    }

    /**
     * create and return a new instance of {@link ConfigurationFile}
     * 
     * @param filePath
     *            path of the YAML file to create
     * @return a new instance of {@link ConfigurationFile}
     * @throws AbstractAgentException
     *             if can't create the yaml file, this exception is thrown
     */
    static ConfigurationFile create(Path filePath) throws AbstractAgentException {
        // check file path
        assertIsTrue(Objects.nonNull(filePath), excClass, LOGGER, "no file path given");
        // create a new instance of yaml file
        ConfigurationFile yamlFile = new ConfigurationFile(filePath);
        assertIsTrue(yamlFile.createFile(), excClass, LOGGER, "can't create file: " + filePath);
        LOGGER.info("created yaml file '{}' successfully", filePath);
        return yamlFile;
    }

    /**
     * get a new instance of {@link ConfigurationFile} as in memory file with given content
     * 
     * @param content
     *            of the configuration
     * @return a new instance of {@link ConfigurationFile}
     * @throws AbstractAgentException
     *             if can't open the yaml file, this exception is thrown
     */
    static ConfigurationFile open(String content) throws AbstractAgentException {
        // check content
        assertIsTrue(!StringUtils.isEmpty(content), excClass, LOGGER, "no content given to open yaml file");
        // create a new instance (no file path) and try to read from given content
        ConfigurationFile yamlFile = new ConfigurationFile(content);
        assertIsTrue(yamlFile.fillRootNodeFromContent(content), excClass, LOGGER, "can't open yaml file from content");
        LOGGER.info("opened yaml file from content successfully");
        return yamlFile;
    }

    /**
     * get current set file path
     * 
     * @return current set file path
     */
    Path getFilePath() {
        return this.filePath;
    }

    /**
     * save the current state of this YAML file at current set file path
     * 
     * @return whether saving file has been successful
     */
    boolean save() {
        return save(this.filePath);
    }

    /**
     * save the current state of this YAML file at given file path
     * 
     * @param filePath
     *            path of the file to save
     * @return whether saving file has been successful
     */
    boolean save(Path filePath) {
        try (FileOutputStream stream = new FileOutputStream(filePath.toFile())) {
        	// write content to file (overwrite)
            stream.write(content.getBytes());
            this.filePath = filePath;
            return true;
        } catch (Exception e) {
            LOGGER.error("can't save YAML file at '{}' reason: {}", filePath, e);
            return false;
        } 
    }

    /**
     * get a configuration of given type from the already opened YAML file; the configuration class to get can be
     * annotated by '@ConfigurationPath'
     * 
     * @param configurationType
     *            type of configuration to read (can be annotated by '@ConfigurationPath')
     * @return the configuration read from YAML file or {@code null} if file can't be read
     * @throws AbstractAgentException
     */
    <T extends Configuration> T getConfiguration(Class<T> configurationType) throws AbstractAgentException {
        // check type
        assertIsTrue(Objects.nonNull(configurationType), excClass, LOGGER, "no configuration type given");
        // get tokens of annotated configuration path (if exists)
        // and get child node by path tokens
        String[] pathTokens = getPathTokens(configurationType);
        assertIsTrue(Objects.nonNull(pathTokens) && pathTokens.length > 0, excClass, LOGGER, "can't find path tokens");
        // don't create not existing nodes
        ObjectNode childNode = getChildNodeByPathTokens(pathTokens, false);
        assertIsTrue(Objects.nonNull(childNode), excClass, LOGGER, "can't find child node");
        // read configuration
        T conf = getConfigurationFromNode(childNode, configurationType);
        assertIsTrue(Objects.nonNull(conf), excClass, LOGGER, "could not create configuration " + configurationType);
        return conf;
    }

    /**
     * put the given configuration into the YAML file; existing configuration properties out of scope of the given
     * configuration object are kept unchanged, properties in scope of the configuration object are overwritten
     * 
     * @param configuration
     *            configuration object to write
     * @param saveFile
     *            whether to save the file ({@code true}) or to keep changes only in memory ({@code false})
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
        
        // content needs to be filled from rootNode to save it (comments get lost)
        if(saveFile) {
			try {
				content = yamlMapper.writeValueAsString(rootNode);
			} catch (JsonProcessingException e) {
				LOGGER.error("can't fill root node reason: {}", e);
				return false;
			}
        }
        
        // save file if requested
        return saveFile ? save() : true;
    }

    /**
     * get the content of the YAML file as string
     * 
     * @return content of the YAML file or {@code null} if an error occurs
     */
    String getContent() {
        return content;
    }
    
    /**
     * get the content of the YAML file (represented by current root node) as string
     * 
     * @return content of the YAML file or {@code null} if an error occurs
     */
    String getRootNodeContent() {
		try {
			return yamlMapper.writeValueAsString(rootNode);
		} catch (JsonProcessingException e) {
			LOGGER.error("can't fill root node reason: {}", e);
			return null;
		}
    }

    public boolean existPath(String... pathTokens) {
        // start with root node
        if (Objects.isNull(pathTokens) || pathTokens.length == 0) {
            return false;
        }
        ObjectNode currentNode = rootNode;
        boolean found = true;
        String lastToken = pathTokens[pathTokens.length-1];

        for (String pathToken : pathTokens) {
            // get child node by name 'pathToken' and check 
            Object childNodeObject = currentNode.get(pathToken);
            if (Objects.nonNull(childNodeObject)) {
                if (ObjectNode.class.isInstance(childNodeObject)) {
                    currentNode = ObjectNode.class.cast(childNodeObject);
                } else if (ValueNode.class.isInstance(childNodeObject)) {
                    found = pathToken.equals(lastToken);
                    currentNode = null;
                    break;
                }
            } else {
                found = false;
                currentNode = null;
                break;
            }
        }

        return found;
    }

    /**
     * read a YAML file at current set file path
     */
    private boolean readFile() {
        try(InputStream in = new FileInputStream(filePath.toString())){
            LOGGER.info("read YAML file '{}'", filePath);
            String c = new BufferedReader(new InputStreamReader(in))
    				// to lines
            		.lines()
                    // map line to line plus LF
                    .map(l -> l + '\n')
                    // from list to String stream
                    .flatMap(l -> Stream.of(l))
                    // String stream to String
                    .collect(Collectors.joining());
            // validate content
            if(!fillRootNodeFromContent(c)) return false;
            this.content = c;
            return true;
        } catch (Exception e) {
            LOGGER.error("can't read YAML file '{}' reason: {}", filePath, e);
            return false;
        }
    }

    /**
     * read the given content string and fill root node
     */
    private boolean fillRootNodeFromContent(String c) {
        try {
            LOGGER.info("fill root node from content string");
            rootNode = (ObjectNode) yamlMapper.readTree(c);
            if (Objects.isNull(rootNode)) {
                LOGGER.debug("content is empty, create new configuration root node");
                rootNode = yamlMapper.createObjectNode();
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("can't fill root node from content", e);
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
            Files.createFile(filePath);
        } catch (IOException e) {
            LOGGER.error("can't create yaml file '{}' reason: {}", filePath, e);
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
        return pathTokens.toArray(new String[] {});
    }

    /**
     * traverses a node tree, beginning at 'rootNode' by the names given at 'pathTokens'; if 'createMissingNodes' is
     * true, not existing child nodes are created
     */
    private ObjectNode getChildNodeByPathTokens(String[] pathTokens, boolean createMissingNodes) {
        // start with root node
        ObjectNode parentNode = rootNode;
        ObjectNode childNode = rootNode;
        for (String pathToken : pathTokens) {
            // get child node by name 'pathToken' and check 
            Object childNodeObject = parentNode.get(pathToken);
            if (Objects.nonNull(childNodeObject) && ObjectNode.class.isInstance(childNodeObject)) {
                childNode = ObjectNode.class.cast(childNodeObject);
            } else {
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
            LOGGER.error("can't create configuration of type '{}' reason: {}", type, e);
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
