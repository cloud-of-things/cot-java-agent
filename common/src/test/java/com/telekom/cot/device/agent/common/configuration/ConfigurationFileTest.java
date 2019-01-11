package com.telekom.cot.device.agent.common.configuration;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.annotations.ConfigurationPath;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;

public class ConfigurationFileTest {

    @Mock
    private Logger mockLogger;
    private ConfigurationFile configurationFile;

    @Before
    public void setUp() throws Exception {
        URI pathUri = ConfigurationFileTest.class.getResource("/agent.yaml").toURI();
        configurationFile = ConfigurationFile.open(Paths.get(pathUri));
        // initialize and inject mocks
        MockitoAnnotations.initMocks(this);
        InjectionUtil.injectStatic(ConfigurationFile.class, mockLogger);
        // check setUp
        ConfigurationAgent configurationAgent = configurationFile.getConfiguration(ConfigurationAgent.class);
        assertThat(configurationAgent.isBooleanProp(), Matchers.equalTo(true));
        assertThat(configurationAgent.getIntProp(), Matchers.equalTo(1));
        assertThat(configurationAgent.getStringProp(), Matchers.equalTo("text"));
    }

    @Test
    public void test() {
        assertThat(configurationFile.existPath(new String[] { "agent" }), Matchers.equalTo(true));
        assertThat(configurationFile.existPath(new String[] { "agent", "booleanProp" }), Matchers.equalTo(true));
        assertThat(configurationFile.existPath(new String[] { "agent", "not" }), Matchers.equalTo(false));
        assertThat(configurationFile.existPath(new String[] {}), Matchers.equalTo(false));
    }
    
    /**
     * filePath is null
     */
    @Test
    public void testOpenByPathFilePathIsNull() throws URISyntaxException, AbstractAgentException, IOException {
        Path filePath = null;
        try {
            ConfigurationFile.open(filePath);
            Assert.fail();
        } catch (AbstractAgentException exc) {
            // ignore
        }
        verify(mockLogger, times(1)).error("no file path given");
    }

    /**
     * is not a valid YAML file
     */
    @Test
    public void testOpenByPathUnvalidYamlFile() throws URISyntaxException, AbstractAgentException, IOException {
        URI pathUri = ConfigurationFileTest.class.getResource("/isnot.yaml").toURI();
        Path filePath = Paths.get(pathUri);
        try {
            ConfigurationFile.open(filePath);
            Assert.fail();
        } catch (AbstractAgentException exc) {
            // ignore
        }
        verify(mockLogger, times(1)).error("can't read file: " + filePath);
    }

    /**
     * file not exist
     */
    @Test
    public void testOpenByPathFileNotExist() throws URISyntaxException, AbstractAgentException, IOException {
        String workingDir = System.getProperty("user.dir");
        Path filePath = Files.createTempDirectory(Paths.get(workingDir, "target"), "notexist");
        try {
            ConfigurationFile.open(filePath);
            Assert.fail();
        } catch (AbstractAgentException exc) {
            // ignore
        } finally {
            Files.delete(filePath);
        }
        verify(mockLogger, times(1)).error("can't read file: " + filePath);
    }

    /**
     * content is null
     */
    @Test
    public void testOpenByContentContentIsNull() throws URISyntaxException, AbstractAgentException, IOException {
        String content = null;
        try {
            ConfigurationFile.open(content);
            Assert.fail();
        } catch (AbstractAgentException exc) {
            // ignore
        }
        verify(mockLogger, times(1)).error("no content given to open yaml file");
    }

    /**
     * is not a valid YAML content
     */
    @Test
    public void testOpenByContentUnvalidYamlContent() throws URISyntaxException, AbstractAgentException, IOException {
        String content = "this is not a yaml content";
        try {
            ConfigurationFile.open(content);
            Assert.fail();
        } catch (AbstractAgentException exc) {
            // ignore
        }
        verify(mockLogger, times(1)).error("can't open yaml file from content");
    }

    /**
     * path is null
     */
    @Test
    public void testCreateByPathIsNull() {
        Path filePath = null;
        try {
            ConfigurationFile.create(filePath);
            Assert.fail();
        } catch (AbstractAgentException exc) {
            // ignore
        }
        verify(mockLogger, times(1)).error("no file path given");
    }

    /**
     * path not exist
     */
    @Test
    public void testCreateByPathNotExist() throws IOException {
        String workingDir = System.getProperty("user.dir");
        Path filePath = Files.createTempDirectory(Paths.get(workingDir, "target"), "notexist");
        try {
            ConfigurationFile.create(filePath);
            Assert.fail();
        } catch (AbstractAgentException exc) {
            // ignore
        } finally {
            Files.delete(filePath);
        }
        verify(mockLogger, times(1)).error("can't create file: " + filePath);
    }

    /**
     * successful case
     */
    @Test
    public void testCreateByPath() throws IOException, AbstractAgentException {
        String workingDir = System.getProperty("user.dir");
        Path dir = Paths.get(workingDir, "target", "test");
        Files.createDirectories(dir);
        Path filePath = Paths.get(dir.toAbsolutePath().toString(), "agent.yaml");
        try {
            ConfigurationFile confFile = ConfigurationFile.create(filePath);
            Assert.assertThat(confFile.getContent(), Matchers.equalTo(null));
        } finally {
            Files.delete(filePath);
            Files.delete(dir);
        }
        verify(mockLogger, times(1)).info("created yaml file '{}' successfully", filePath);
    }

    @Test
    public void testPutConfigurationAndSaveFalse() throws IOException, AbstractAgentException {
        String workingDir = System.getProperty("user.dir");
        Path dir = Paths.get(workingDir, "target", "test");
        Files.createDirectories(dir);
        Path filePath = Paths.get(dir.toAbsolutePath().toString(), "agent.yaml");
        try {
            ConfigurationFile confFileA = ConfigurationFile.create(filePath);
            ConfigurationAgent configurationAgent = new ConfigurationAgent();
            configurationAgent.setBooleanProp(true);
            configurationAgent.setIntProp(1);
            configurationAgent.setStringProp("text");
            confFileA.putConfiguration(configurationAgent, false);
            String expected = "---\n" + //
            				"agent:\n" + //
                            "  booleanProp: true\n" + //
                            "  intProp: 1\n" + //
                            "  stringProp: \"text\"\n";
            Assert.assertThat(confFileA.getRootNodeContent(), Matchers.equalTo(expected));
            ConfigurationFile confFileB = ConfigurationFile.open(filePath);
            Assert.assertThat(confFileA.getContent(), Matchers.not(Matchers.equalTo(confFileB.getContent())));
        } finally {
            Files.delete(filePath);
            Files.delete(dir);
        }
        verify(mockLogger, times(1)).info("created yaml file '{}' successfully", filePath);
    }

    @Test
    public void testPutConfigurationAndSaveTrue() throws IOException, AbstractAgentException {
        String workingDir = System.getProperty("user.dir");
        Path dir = Paths.get(workingDir, "target", "test");
        Files.createDirectories(dir);
        Path filePath = Paths.get(dir.toAbsolutePath().toString(), "agent.yaml");
        try {
            ConfigurationFile confFileA = ConfigurationFile.create(filePath);
            ConfigurationAgent configurationAgent = new ConfigurationAgent();
            configurationAgent.setBooleanProp(true);
            configurationAgent.setIntProp(1);
            configurationAgent.setStringProp("text");
            confFileA.putConfiguration(configurationAgent, true);
            String expected = "---\n" + //
                            "agent:\n" + //
                            "  booleanProp: true\n" + //
                            "  intProp: 1\n" + //
                            "  stringProp: \"text\"\n";
            Assert.assertThat(confFileA.getContent(), Matchers.equalTo(expected));
            ConfigurationFile confFileB = ConfigurationFile.open(filePath);
            Assert.assertThat(confFileA.getContent(), Matchers.equalTo(confFileB.getContent()));
        } finally {
            Files.delete(filePath);
            Files.delete(dir);
        }
        verify(mockLogger, times(1)).info("created yaml file '{}' successfully", filePath);
    }

    @Test
    public void testPutConfigurationAndSaveFalseWithError() throws IOException, AbstractAgentException {
        String workingDir = System.getProperty("user.dir");
        Path dir = Paths.get(workingDir, "target", "test");
        Files.createDirectories(dir);
        Path filePath = Paths.get(dir.toAbsolutePath().toString(), "agent.yaml");
        Path filePathNotExist = Files.createTempDirectory(Paths.get(workingDir, "target"), "notexist");
        try {
            ConfigurationFile confFileA = ConfigurationFile.create(filePath);
            ConfigurationAgent configurationAgent = new ConfigurationAgent();
            configurationAgent.setBooleanProp(true);
            configurationAgent.setIntProp(1);
            configurationAgent.setStringProp("text");
            confFileA.putConfiguration(configurationAgent, false);
            Assert.assertThat(confFileA.getContent(), Matchers.equalTo(null));
            Assert.assertThat(confFileA.save(filePathNotExist), Matchers.equalTo(false));
        } finally {
            Files.delete(filePath);
            Files.delete(filePathNotExist);
            Files.delete(dir);
        }
        verify(mockLogger, times(1)).info("created yaml file '{}' successfully", filePath);
    }

    /**
     * type is null
     */
    @Test
    public void testGetConfigurationTypeIsNull() {
        try {
            configurationFile.getConfiguration(null);
            Assert.fail();
        } catch (AbstractAgentException exc) {
            // ignore
        }
        verify(mockLogger).error("no configuration type given");
    }

    /**
     * no annotation
     */
    @Test
    public void testGetConfigurationNoAnnotation() {
        try {
            configurationFile.getConfiguration(ConfigurationNoAnnotation.class);
            Assert.fail();
        } catch (AbstractAgentException exc) {
            // ignore
        }
        verify(mockLogger).warn("can't find annotation '@ConfigurationPath' at class '{}'", //
                        ConfigurationNoAnnotation.class.getName());
        verify(mockLogger).error("can't find path tokens");
    }

    /**
     * empty annotation
     */
    @Test
    public void testGetConfigurationEmptyAnnotation() {
        try {
            configurationFile.getConfiguration(ConfigurationEmptyAnnotation.class);
            Assert.fail();
        } catch (AbstractAgentException exc) {
            // ignore
        }
        verify(mockLogger).warn("no configuration path given at '@ConfigurationPath' annotation");
        verify(mockLogger, times(1)).error("can't find path tokens");
    }

    /**
     * path token not exist
     */
    @Test
    public void testGetConfigurationErrorCases() {
        try {
            configurationFile.getConfiguration(ConfigurationPathTokenNotExist.class);
            Assert.fail();
        } catch (AbstractAgentException exc) {
            // ignore
        }
        verify(mockLogger, times(1)).error("can't find child node");
    }

    static class ConfigurationNoAnnotation implements Configuration {
    }

    @ConfigurationPath("")
    static class ConfigurationEmptyAnnotation implements Configuration {
    }

    @ConfigurationPath("agent.missing")
    static class ConfigurationPathTokenNotExist implements Configuration {
    }

    @ConfigurationPath("agent")
    static class ConfigurationAgent implements Configuration {

        private boolean booleanProp;
        private int intProp;
        private String stringProp;

        public boolean isBooleanProp() {
            return booleanProp;
        }

        public void setBooleanProp(boolean booleanProp) {
            this.booleanProp = booleanProp;
        }

        public int getIntProp() {
            return intProp;
        }

        public void setIntProp(int intProp) {
            this.intProp = intProp;
        }

        public String getStringProp() {
            return stringProp;
        }

        public void setStringProp(String stringProp) {
            this.stringProp = stringProp;
        }
    }
}
