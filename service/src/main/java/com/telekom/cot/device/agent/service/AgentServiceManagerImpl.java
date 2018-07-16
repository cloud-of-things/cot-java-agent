package com.telekom.cot.device.agent.service;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentServiceNotFoundException;
import com.telekom.cot.device.agent.service.event.AgentContext;
import com.telekom.cot.device.agent.service.event.AgentContextImpl;
import com.telekom.cot.device.agent.service.injection.DependencyInjector;

/**
 * {@link AgentServiceManagerImpl} manages all agent service implementations found at class path
 *
 */
public final class AgentServiceManagerImpl implements AgentServiceManager {

	// singleton instance
	private static final AgentServiceManagerImpl INSTANCE;
	private static final Logger LOGGER;
	private static final ServiceLoader<AgentService> SERVICE_LOADER;
	private final AgentContext agentContext;

	private List<AgentServiceProxy> agentServiceProxies;

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
		agentServiceProxies = new ArrayList<>();
		agentContext = new AgentContextImpl();
	}

	/**
	 * gets the singleton instance of {@link AgentServiceManagerImpl}
	 */
	public static AgentServiceManager getInstance() {
		return INSTANCE;
	}

	@Override
	public int count() {
		return agentServiceProxies.size();
	}

	/**
	 * {@inheritDoc}
	 * @throws AbstractAgentException 
	 */
	@Override
	public void loadAndInitServices(ConfigurationManager configManager, AgentCredentialsManager credentialsManager) throws AbstractAgentException {
		LOGGER.debug("start to load and initialize agent services");

		// clear current list of service proxies, reload service loader and get loaded services
		agentServiceProxies.clear();
        List<AgentService> loadedServices = reloadServices();

		// get new dependency injector and inject configurations, configuration manager, agent service provider and agent services
		DependencyInjector dependencyInjector = DependencyInjector.getInstance(this, configManager, credentialsManager); 
		loadedServices = dependencyInjector.injectConfigurations(loadedServices);
        loadedServices = dependencyInjector.injectConfigurationManager(loadedServices);
        loadedServices = dependencyInjector.injectAgentServiceProvider(loadedServices);
        loadedServices = dependencyInjector.injectCredentialsManager(loadedServices);
        loadedServices = dependencyInjector.injectAgentServices(loadedServices);
		
		// create service proxies and call init()
        for (AgentService service : loadedServices) {
            // create a proxy of the original service and init service 
            AgentServiceProxy agentServiceProxy = AgentServiceProxy.create(service);
            service.init(agentContext);
            
            // add only valid service to list
            agentServiceProxies.add(agentServiceProxy);
            LOGGER.debug("loaded agent service '{}' successfully", service.getClass());
        }
		
		// create a new list (copy of the 'agentServices' list)
		LOGGER.info("loaded and initialized {} agent services", agentServiceProxies.size());
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
        assertNotNull(serviceType, AgentServiceNotFoundException.class, LOGGER, "service type not given");
        assertIsTrue(serviceType.isInterface(), AgentServiceNotFoundException.class, LOGGER, "sevice type must be a interface");

		// search for services of given type
		ArrayList<T> foundServices = new ArrayList<>();
		for (AgentServiceProxy agentServiceProxy : agentServiceProxies) {
			if (agentServiceProxy.isInstance(serviceType)) {
				LOGGER.debug("found service '{}'", agentServiceProxy);
				foundServices.add(agentServiceProxy.getProxy());
			}
		}

        // check found services
		assertIsTrue(!foundServices.isEmpty(), AgentServiceNotFoundException.class, LOGGER, "found no service of type " + serviceType.getName());

		LOGGER.info("found {} agent services of type '{}'", foundServices.size(), serviceType);
		return foundServices;
	}
	
    /**
     * clear list of service proxies, reload service loader and get all loaded agent services 
     */
    private List<AgentService> reloadServices() {
        // reload service loader and get all services from service loader
        SERVICE_LOADER.reload();
        List<AgentService> loadedServices = new ArrayList<>();
        for (AgentService service : SERVICE_LOADER) {
            // add service if not null
            if (Objects.nonNull(service)) {
                loadedServices.add(service);
            }
        }
        
        return loadedServices;
    }
        
}
