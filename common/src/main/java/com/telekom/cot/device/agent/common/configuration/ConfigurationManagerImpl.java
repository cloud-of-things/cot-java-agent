package com.telekom.cot.device.agent.common.configuration;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.assertIsTrue;
import static com.telekom.cot.device.agent.common.util.AssertionUtil.assertNotNull;
import static com.telekom.cot.device.agent.common.util.AssertionUtil.createExceptionAndLog;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.ConfigurationNotFoundException;
import com.telekom.cot.device.agent.common.exc.ConfigurationUpdateException;
import com.telekom.cot.device.agent.common.util.AssertionUtil;
import com.telekom.cot.device.agent.common.util.ValidationUtil;

public class ConfigurationManagerImpl implements ConfigurationManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManagerImpl.class);
	
	private ConfigurationFile configurationFile;
	private HashMap<Class<? extends Configuration>, Configuration> configurations;
	
    private ConfigurationManagerImpl(Path configurationFilePath) throws AbstractAgentException {
        configurationFile = ConfigurationFile.open(configurationFilePath);
        configurations = new HashMap<>();
    }

	public static ConfigurationManager getInstance(Path configurationFile) throws AbstractAgentException {
		return new ConfigurationManagerImpl(configurationFile);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Configuration> T getConfiguration(Class<T> configurationType) throws AbstractAgentException {
		// check configuration type
		assertNotNull(configurationType, ConfigurationNotFoundException.class, LOGGER, "no configuration type given");
		LOGGER.info("get configuration of type '{}'", configurationType);
		
		// check if configuration exists in map
		T configuration = null;
		if (configurations.containsKey(configurationType)) {
			configuration = configurationType.cast(configurations.get(configurationType));
		}
		
		// read configuration from file if null
		if (Objects.isNull(configuration)) {
			configuration = readConfigurationFromFile(configurationType);
		}

		LOGGER.info("got configuration of type '{}' successfully", configurationType);
		return configuration;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getConfiguration() throws AbstractAgentException {
		String configuration = configurationFile.getContent();
		assertNotNull(configuration, ConfigurationNotFoundException.class, LOGGER, "can't get complete configuration as string");
		
		LOGGER.info("got complete configuration successfully");
		return configuration;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateConfiguration(Configuration configuration, boolean persist) throws AbstractAgentException {
		// check configuration
		assertNotNull(configuration, ConfigurationUpdateException.class, LOGGER, "no configuration given");

		// put configuration to YAML file and persist (if wanted) 
		Class<? extends Configuration> configurationType = configuration.getClass();
		if (!configurationFile.putConfiguration(configuration, persist)) {
			throw createExceptionAndLog(ConfigurationUpdateException.class, LOGGER, "can't persist configuration of type "
															+ configurationType.getName());
		}

		configurations.put(configurationType, configuration);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateConfiguration(String configuration) throws AbstractAgentException {
		// open temporary in memory yaml file and check
		ConfigurationFile tempYamlFile = ConfigurationFile.open(configuration);
		assertNotNull(tempYamlFile, ConfigurationUpdateException.class, LOGGER, "can't open temporary YAML file with given configuration content");

		// try to read all current configurations from temporary yaml file
		for(Class<? extends Configuration> configurationType : configurations.keySet()) {
			// read configuration of current type from temporary yaml file and check 
			Configuration newConfiguration = tempYamlFile.getConfiguration(configurationType);
			assertNotNull(newConfiguration, ConfigurationUpdateException.class, LOGGER, "new configuration contains no configuration of type '"
												+ configurationType.getName() + "'");
		}

		// save temporary YAML file
		Path filePath = configurationFile.getFilePath();
		assertIsTrue(tempYamlFile.save(filePath), ConfigurationUpdateException.class, LOGGER,
					"can't save updated configuration at '" + filePath + "'");

		LOGGER.info("updated configuration successfully");
	}
	
	/**
     * {@inheritDoc}
     */
	@Override
	public boolean existPath(String... pathTokens) throws AbstractAgentException {
	    assertNotNull(pathTokens, ConfigurationNotFoundException.class, LOGGER, "path tokens are null");
	    return configurationFile.existPath(pathTokens);
	}
	
	/**
	 * reads a configuration of given type from configuration / credentials file
	 */
	private <T extends Configuration> T readConfigurationFromFile(Class<T> configurationType) throws AbstractAgentException {
		LOGGER.debug("read configuration of type '{}' from file", configurationType);
		
		// read configuration from file and check
		T configuration = configurationFile.getConfiguration(configurationType);
		AssertionUtil.assertNotNull(configuration, ConfigurationNotFoundException.class, LOGGER, "configuration of type '" + configurationType.getName() + "' not found");
		
		// validate configuration
		if (!ValidationUtil.isValid(configuration)) {
			throw AssertionUtil.createExceptionAndLog(ConfigurationNotFoundException.class, LOGGER, "configuration read from file is not valid");
		}

		LOGGER.info("read configuration of type '{}' from file successfully", configurationType);
		configurations.put(configurationType, configuration);
		return configuration;
	}
}
