package com.telekom.cot.device.agent.service;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentServiceNotFoundException;
import com.telekom.cot.device.agent.service.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.service.configuration.ConfigurationManager;

public abstract class AbstractAgentService implements AgentService {

	private static final String ERROR_NO_SERVICE_PROVIDER = "no service provider given";
	private static final String ERROR_NO_CONFIGURATION_MANAGER = "no configuration manager given";
	private static final String ERROR_NO_CREDENTIALS_MANAGER = "no agent credentials manager given";

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAgentService.class);

	private AgentServiceProvider serviceProvider;
	private ConfigurationManager configurationManager;
	private AgentCredentialsManager agentCredentialsManager;
    private boolean started = false;

	/**
	 * gets the service provider instance
	 */
	protected AgentServiceProvider getServiceProvider() throws AbstractAgentException {
        // check agent service provider
        assertNotNull(serviceProvider, ERROR_NO_SERVICE_PROVIDER);
		return serviceProvider;
	}

	/**
	 * gets the configuration manager instance
	 */
	protected ConfigurationManager getConfigurationManager() throws AbstractAgentException {
		// check configuration manager
		assertNotNull(configurationManager, ERROR_NO_CONFIGURATION_MANAGER);
		return configurationManager;
	}

	/**
	 * gets the agent credentials manager instance
	 */
	protected AgentCredentialsManager getAgentCredentialsManager() throws AbstractAgentException {
		// check agent credentials manager
		assertNotNull(agentCredentialsManager, ERROR_NO_CREDENTIALS_MANAGER);
		return agentCredentialsManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(AgentServiceProvider serviceProvider, ConfigurationManager configurationManager, AgentCredentialsManager agentCredentialsManager) throws AbstractAgentException {
		LOGGER.debug("init service");

		// check service provider and configuration manager
		assertNotNull(serviceProvider, ERROR_NO_SERVICE_PROVIDER);
		assertNotNull(configurationManager, ERROR_NO_CONFIGURATION_MANAGER);
		assertNotNull(agentCredentialsManager, ERROR_NO_CREDENTIALS_MANAGER);

		this.serviceProvider = serviceProvider;
		this.configurationManager = configurationManager;
		this.agentCredentialsManager = agentCredentialsManager;
	}

	/**
	 * {@inheritDoc}
	 */
    @Override
    public void start() throws AbstractAgentException {
        started = true;
    }
    
	/**
	 * {@inheritDoc}
	 */
    @Override
    public void stop() throws AbstractAgentException {
        started = false;
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public boolean isStarted() {
        return started;
    }

	/**
	 * gets an agent service of given service type
	 * @param <T> type of the agent service to get
	 * @param serviceType type representation of the agent service to get
	 * @return an agent service of given type
	 * @throws AbstractAgentException if not exactly one service of given type found
	 */
	public <T extends AgentService> T getService(Class<T> serviceType) throws AbstractAgentException {
		if (Objects.isNull(serviceProvider)) {
			throw new AgentServiceNotFoundException(ERROR_NO_SERVICE_PROVIDER);
		}
		
		return serviceProvider.getService(serviceType);
	}

	/**
	 * gets a list of agent services of given service type
	 * @param <T> type of the agent service to get
	 * @param serviceType type representation of the agent service to get
	 * @return a list of agent services of given type
	 * @throws AbstractAgentException if no services of given type found 
	 */
	public <T extends AgentService> List<T> getServices(Class<T> serviceType) throws AbstractAgentException {
		if (Objects.isNull(serviceProvider)) {
			throw new AgentServiceNotFoundException(ERROR_NO_SERVICE_PROVIDER);
		}

		return serviceProvider.getServices(serviceType);
	}
	
	@SuppressWarnings("serial")
	private void assertNotNull(Object object, String errorMessage) throws AbstractAgentException {
		if(Objects.isNull(object)) {
			LOGGER.error(errorMessage);
			throw new AbstractAgentException(errorMessage) {};
		}
	}
}
