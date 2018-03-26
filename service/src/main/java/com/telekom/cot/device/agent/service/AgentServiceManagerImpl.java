package com.telekom.cot.device.agent.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentServiceNotFoundException;
import com.telekom.cot.device.agent.service.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.service.configuration.ConfigurationManager;

/**
 * {@link AgentServiceManagerImpl} manages all agent service implementations found at class path
 *
 */
public final class AgentServiceManagerImpl implements AgentServiceManager {

	// singleton instance
	private static final AgentServiceManagerImpl INSTANCE;
	private static final Logger LOGGER;
	private static final ServiceLoader<AgentService> SERVICE_LOADER;

	private List<AgentService> agentServices;

	/**
	 * initialize the singleton instance
	 */
	static {
		LOGGER = LoggerFactory.getLogger(AgentServiceManagerImpl.class);
		SERVICE_LOADER = ServiceLoader.load(AgentService.class);
		INSTANCE = new AgentServiceManagerImpl();
	}

	// private constructor (singleton)
	private AgentServiceManagerImpl() {
		agentServices = new ArrayList<>();
	}

	/**
	 * gets the singleton instance of {@link AgentServiceManagerImpl}
	 */
	public static AgentServiceManager getInstance() {
		return INSTANCE;
	}

	@Override
	public int count() {
		return agentServices.size();
	}

	/**
	 * {@inheritDoc}
	 * @throws AbstractAgentException 
	 */
	@Override
	public void loadAndInitServices(ConfigurationManager configurationManager, AgentCredentialsManager agentCredentialsManager) throws AbstractAgentException {
		LOGGER.debug("start to load and initialize agent services");

		// clear current list of services and reload service loader
		agentServices.clear();
		SERVICE_LOADER.reload();

		// get all services from service loader
		for (AgentService service : SERVICE_LOADER) {
			// check service
			if (Objects.isNull(service)) {
				continue;
			}

			// initialize service
			if (AbstractAgentService.class.isInstance(service)) {
				LOGGER.debug("initialize agent service '{}'", service.getClass());
				((AbstractAgentService) service).init(this, configurationManager, agentCredentialsManager);
			}
			
			agentServices.add(service);
			LOGGER.debug("loaded agent service '{}'", service.getClass());
		}

		// create a new list (copy of the 'agentServices' list)
		LOGGER.info("loaded and initialized {} agent services", agentServices.size());
	}

	/**
	 * {@inheritDoc}
	 */
	public <T extends AgentService> T getService(Class<T> serviceType) throws AbstractAgentException {
		// get services and check count
		List<T> services = getServices(serviceType);
		if (services.size() > 1) {
			LOGGER.error("found more than one instance of '{}'", serviceType);
			throw new AgentServiceNotFoundException("found more than one instance of service type " + serviceType.getName());
		}

		return services.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T extends AgentService> List<T> getServices(Class<T> serviceType) throws AbstractAgentException {
		// check service type
		if (Objects.isNull(serviceType)) {
			LOGGER.error("can't get services without a service type");
			throw new AgentServiceNotFoundException("service type not given");
		}

		// search for services of given type
		ArrayList<T> foundServices = new ArrayList<>();
		for (AgentService service : agentServices) {
			if (serviceType.isInstance(service)) {
				LOGGER.debug("found service '{}'", service.getClass());
				foundServices.add(serviceType.cast(service));
			}
		}

		// check found services
		if(foundServices.isEmpty()) {
			LOGGER.error("found no service of type {}", serviceType);
			throw new AgentServiceNotFoundException("found no service of type " + serviceType.getName());
		}

		LOGGER.info("found {} agent services of type '{}'", foundServices.size(), serviceType);
		return foundServices;
	}
}
