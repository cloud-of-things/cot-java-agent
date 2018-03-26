package com.telekom.cot.device.agent.service.configuration;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;

public interface ConfigurationManager {

	/**
	 * gets a configuration object of given type 
	 * @param configurationType type of configuration to get
	 * @return the requested configuration if available
	 * @throws AbstractAgentException if configuration of given type can't be found
	 */
	public <T extends Configuration> T getConfiguration(Class<T> configurationType) throws AbstractAgentException;

	/**
	 * gets the complete configuration (agent.yaml) as string
	 * @return the complete configuration
	 * @throws AbstractAgentException if configuration can't be represented as string
	 */
	public String getConfiguration() throws AbstractAgentException;
	
	/**
	 * updates the given configuration in the configuration manager
	 * and persists it in the configuration file, if 'persist' is true
	 * @param configuration configuration to update
	 * @param persist tells whether to update persist the configuration or not
	 * @throws AbstractAgentException if an error occurs at update
	 */
	public void updateConfiguration(Configuration configuration, boolean persist) throws AbstractAgentException;

	/**
	 * updates the given configuration in the configuration manager
	 * and persists it in the configuration file
	 * @param configuration configuration to update
	 * @throws AbstractAgentException if an error occurs at update
	 */
	public default void updateConfiguration(Configuration configuration) throws AbstractAgentException {
		updateConfiguration(configuration, true);
	}
	
	/**
	 * updates the complete configuration with the given configuration content (YAML format)  
	 * @param configuration the configuration content in YAML format
	 * @throws AbstractAgentException if configuration can't be updated by given content
	 */
	public void updateConfiguration(String configuration) throws AbstractAgentException;
}
