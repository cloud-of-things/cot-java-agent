package com.telekom.cot.device.agent.service.injection;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.common.configuration.Configuration;
import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.injection.Inject;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.service.AgentService;
import com.telekom.cot.device.agent.service.AgentServiceProvider;

/**
 * {@link DependencyInjector} is used to inject {@link Configuration} and {@link AgentService} instances into services.
 * The member fields have to be annotated by {@link Inject} to get a instance injected.
 *
 */
public class DependencyInjector {
    
    private static final String ERROR_NO_SERVICE_LIST = "no list of agent services given";

    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyInjector.class);
    
    private AgentServiceProvider serviceProvider;
    private ConfigurationManager configurationManager;
    private AgentCredentialsManager credentialsManager;
    
    /**
     * get a new instance of {@link DependencyInjector}
     * @param serviceProvider a valid agent service provider instance
     * @param configurationManager a valid configuration manager instance
     * @return a new created {@link DependencyInjector} instance
     * @throws AbstractAgentException if service provider or configuration manager instances are not valid
     */
    public static DependencyInjector getInstance(AgentServiceProvider serviceProvider,
                    ConfigurationManager configurationManager, AgentCredentialsManager credentialsManager)
                                    throws AbstractAgentException {
        // check service provider, configuration manager and credentialsManager
        assertNotNull(serviceProvider, DependencyInjectorException.class, LOGGER, "can't get dependency injector instance, no service provider given");
        assertNotNull(configurationManager, DependencyInjectorException.class, LOGGER, "can't get dependency injector instance, no configuration manager given");
        assertNotNull(credentialsManager, DependencyInjectorException.class, LOGGER, "can't get dependency injector instance, no agent credentials manager given");
        return new DependencyInjector(serviceProvider, configurationManager,credentialsManager);
    }
    
    /**
     * private constructor, use 'getInstance'
     */
    private DependencyInjector(AgentServiceProvider serviceProvider, ConfigurationManager configurationManager,
                    AgentCredentialsManager credentialsManager) {
        this.serviceProvider = serviceProvider;
        this.configurationManager = configurationManager;
        this.credentialsManager = credentialsManager;
    }
    
    /**
     * inject all configurations into the given service;
     * configuration member fields at given service have to be a (sub-) type of {@link Configuration}
     * and must be annotated by {@link Inject}  
     * @param service the service to get his configuration(s) injected
     * @throws AbstractAgentException if an error occurs
     */
    public void injectConfigurations(AgentService service) throws AbstractAgentException {
        // get all fields of type 'Configuration' from 'service', annotated with '@Inject' 
        List<Field> fields = getAnnotatedFields(service, Configuration.class);
        if (fields.isEmpty()) {
            return;
        }

        LOGGER.debug("inject configurations into service '{}'", service);
        for (Field field : fields) {
            // get and inject configuration
            Configuration configuration = configurationManager.getConfiguration(field.getType().asSubclass(Configuration.class));
            LOGGER.debug("inject configuration '{}' into service '{}', member field '{}'", configuration, service, field.getName());
            if(!InjectionUtil.inject(service, field.getName(), configuration)) {
                throw createExceptionAndLog(DependencyInjectorException.class, LOGGER,
                                "can't inject configuration '" + configuration + "' into service '" + service + "'");
            }
        }
        
        LOGGER.info("injected configurations into service '{}' successfully", service);
    }

    /**
     * inject all configurations into all given services;
     * configuration member fields at each service have to be a (sub-) type of {@link Configuration}
     * and must be annotated by {@link Inject}  
     * @param services the services to get their configuration(s) injected
     * @throws AbstractAgentException if an error occurs
     */
    public List<AgentService> injectConfigurations(List<AgentService> services) throws AbstractAgentException {
        assertNotNull(services, DependencyInjectorException.class, LOGGER, ERROR_NO_SERVICE_LIST);
        
        List<AgentService> servicesWithConfiguration = new ArrayList<>();
        for(AgentService service : services) {
            try {
                injectConfigurations(service);
                servicesWithConfiguration.add(service);
            } catch(Exception e) {
                LOGGER.warn("service '{}' not added, can't inject configurations", service);
            }
        }
        
        return servicesWithConfiguration;
    }
    
    /**
     * inject an instance of {@link ConfigurationManager} into the given service;
     * configuration manager member field(s) at given service have to be annotated by {@link Inject}
     * @param service the service to get a configuration manager instance injected
     * @throws AbstractAgentException if an error occurs
     */
    public void injectConfigurationManager(AgentService service) throws AbstractAgentException {
        // get all fields of type 'ConfigurationManager' from 'service', annotated with '@Inject' 
        List<Field> fields = getAnnotatedFields(service, ConfigurationManager.class);
        if (fields.isEmpty()) {
            return;
        }

        LOGGER.debug("inject configuration manager into service '{}'", service);
        for (Field field : fields) {
            // inject configuration manager
            LOGGER.debug("inject configuration manager into service '{}', member field '{}'", service, field.getName());
            if(!InjectionUtil.inject(service, field.getName(), configurationManager)) {
                throw createExceptionAndLog(DependencyInjectorException.class, LOGGER,
                                "can't inject configuration manager into service '" + service + "'");
            }
        }
        
        LOGGER.info("injected configuration manager into service '{}' successfully", service);
    }

    /**
     * inject an instance of {@link ConfigurationManager} into all given services;
     * configuration manager member field(s) at each service have to be annotated by {@link Inject}
     * @param services the services to get a configuration manager instance injected
     * @throws AbstractAgentException if an error occurs
     */
    public List<AgentService> injectConfigurationManager(List<AgentService> services) throws AbstractAgentException {
        assertNotNull(services, DependencyInjectorException.class, LOGGER, ERROR_NO_SERVICE_LIST);
        
        List<AgentService> resultServices = new ArrayList<>();
        for(AgentService service : services) {
            try {
                injectConfigurationManager(service);
                resultServices.add(service);
            } catch(Exception e) {
                LOGGER.warn("service '{}' not added, can't inject configuration manager", service);
            }
        }
        
        return resultServices;
    }
    
    /**
     * inject depending agent services (taken from given service list) into the given service;
     * agent service member field(s) at given service have to be annotated by {@link Inject}
     * @param service the service to get agent service instances injected
     * @param serviceList a list of services used for injection
     * @throws AbstractAgentException if an error occurs
     */
    public void injectAgentServices(AgentService service, List<AgentService> serviceList) throws AbstractAgentException {
        assertNotNull(serviceList, DependencyInjectorException.class, LOGGER, ERROR_NO_SERVICE_LIST);
        
        // get all fields of type 'AgentService' from 'service', annotated with '@Inject' 
        List<Field> fields = getAnnotatedFields(service, AgentService.class);
        if (fields.isEmpty()) {
            return;
        }
        
        LOGGER.debug("inject agent services into service '{}'", service);
        for (Field field : fields) {
            AgentService serviceToInject = getService(field.getType().asSubclass(AgentService.class), serviceList);
            LOGGER.debug("inject agent service '{}' into service '{}', member field '{}'", serviceToInject, service, field.getName());
            if(!InjectionUtil.inject(service, field.getName(), serviceToInject)) {
                throw createExceptionAndLog(DependencyInjectorException.class, LOGGER,
                                "can't inject agent service '" + serviceToInject + "' into service '" + service + "'");
            }
        }
        
        LOGGER.info("injected agent services into service '{}' successfully", service);
    }
    
    /**
     * inject depending agent services (taken from given service list) into all given services;
     * agent service member field(s) at each service have to be annotated by {@link Inject}
     * @param services list of services to get agent service instances injected and used for injecting services
     * @throws AbstractAgentException if an error occurs
     */
    public List<AgentService> injectAgentServices(List<AgentService> services) throws AbstractAgentException {
        assertNotNull(services, DependencyInjectorException.class, LOGGER, ERROR_NO_SERVICE_LIST);

        List<AgentService> resultServices = new ArrayList<>();
        for (AgentService service : services) {
            try {
                injectAgentServices(service, services);
                resultServices.add(service);
            } catch(Exception e) {
                LOGGER.warn("service '{}' not added, can't inject agent services", service);
            }
        }
        
        return resultServices;
    }

    /**
     * inject an instance of {@link AgentServiceProvider} into the given service;
     * agent service provider member field(s) at given service have to be annotated by {@link Inject}
     * @param service the service to get a agent service provider instance injected
     * @throws AbstractAgentException if an error occurs
     */
    public void injectAgentServiceProvider(AgentService service) throws AbstractAgentException {
        // get all fields of type 'AgentServiceProvider' from 'service', annotated with '@Inject' 
        List<Field> fields = getAnnotatedFields(service, AgentServiceProvider.class);
        if (fields.isEmpty()) {
            return;
        }
        
        LOGGER.debug("inject agent service provider into service '{}'", service);
        for (Field field : fields) {
            LOGGER.debug("inject agent service provider into service '{}', member field '{}'", service, field.getName());
            if(!InjectionUtil.inject(service, field.getName(), serviceProvider)) {
                throw createExceptionAndLog(DependencyInjectorException.class, LOGGER,
                                "can't inject agent service provider into service '" + service + "'");
            }
        }
        
        LOGGER.info("injected agent service provider into service '{}' successfully", service);
    }
    
    /**
     * inject an instance of {@link AgentServiceProvider} into all given services;
     * agent service provider member field(s) at each service have to be annotated by {@link Inject}
     * @param services the services to get a agent service provider instance injected
     * @throws AbstractAgentException if an error occurs
     */
    public List<AgentService> injectAgentServiceProvider(List<AgentService> services) throws AbstractAgentException {
        assertNotNull(services, DependencyInjectorException.class, LOGGER, ERROR_NO_SERVICE_LIST);
        
        List<AgentService> resultServices = new ArrayList<>();
        for(AgentService service : services) {
            try {
                injectAgentServiceProvider(service);
                resultServices.add(service);
            } catch(Exception e) {
                LOGGER.warn("service '{}' not added, can't inject agent service provider", service);
            }
        }
        
        return resultServices;
    }
    
    /**
     * inject an instance of {@link AgentCredentialsManager} into the given service;
     * credentials manager member field(s) at given service have to be annotated by {@link Inject}
     * @param service the service to get a credentials manager instance injected
     * @throws AbstractAgentException if an error occurs
     */
    public void injectCredentialsManager(AgentService service) throws AbstractAgentException {
        // get all fields of type 'AgentCredentialsManager' from 'service', annotated with '@Inject' 
        List<Field> fields = getAnnotatedFields(service, AgentCredentialsManager.class);
        if (fields.isEmpty()) {
            return;
        }
        
        LOGGER.debug("inject agent credentials manager into service '{}'", service);
        for (Field field : fields) {
            LOGGER.debug("inject agent credentials manager into service '{}', member field '{}'", service, field.getName());
            if(!InjectionUtil.inject(service, field.getName(), credentialsManager)) {
                throw createExceptionAndLog(DependencyInjectorException.class, LOGGER,
                                "can't inject agent credentials manager into service '" + service + "'");
            }
        }
        
        LOGGER.info("injected agent credentials manager into service '{}' successfully", service);
    }
    
    /**
     * inject an instance of {@link AgentCredentialsManager} into all given services;
     * credentials manager member field(s) at each service have to be annotated by {@link Inject}
     * @param service the services to get a credentials manager instance injected
     * @throws AbstractAgentException if an error occurs
     */
    public List<AgentService> injectCredentialsManager(List<AgentService> services) throws AbstractAgentException {
        assertNotNull(services, DependencyInjectorException.class, LOGGER, ERROR_NO_SERVICE_LIST);
        
        List<AgentService> resultServices = new ArrayList<>();
        for(AgentService service : services) {
            try {
                injectCredentialsManager(service);
                resultServices.add(service);
            } catch(Exception e) {
                LOGGER.warn("service '{}' not added, can't inject agent credentials manager", service);
            }
        }
        
        return resultServices;
    }
    
    /**
     * get annotated fields (@Inject) of given type at given service instance
     */
    private List<Field> getAnnotatedFields(AgentService service, Class<?> fieldType) throws AbstractAgentException {
        // check service
        assertNotNull(service, DependencyInjectorException.class, LOGGER, "no service instance given");

        // get all fields of given type from 'service', annotated with '@Inject' 
        List<Field> fields = InjectionUtil.getFields(service.getClass(), Inject.class, fieldType);
        if (fields.isEmpty()) {
            LOGGER.debug("can't find '@Inject' annotated fields of type '{}' at service '{}'", fieldType.getSimpleName(), service);
        }

        return fields;
    }
    
    /**
     * search for one service instance of given type in given service list
     */
    private <T extends AgentService> AgentService getService(Class<T> serviceType, List<AgentService> services) throws AbstractAgentException {
        // search for services of given type
        ArrayList<T> foundServices = new ArrayList<>();
        for (AgentService service : services) {
            if (serviceType.isInstance(service)) {
                foundServices.add(serviceType.cast(service));
            }
        }

        // check found services and return
        assertIsTrue(foundServices.size() == 1, DependencyInjectorException.class, LOGGER, "found not exactly one service of type '" + serviceType + "'");
        return foundServices.get(0);
    }
}
