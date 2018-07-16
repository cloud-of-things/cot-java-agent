package com.telekom.cot.device.agent.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.configuration.AgentCredentials;
import com.telekom.cot.device.agent.common.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.credentials.DeviceCredentialsService;
import com.telekom.cot.device.agent.event.EventService;
import com.telekom.cot.device.agent.inventory.InventoryService;
import com.telekom.cot.device.agent.service.AgentService;
import com.telekom.cot.device.agent.service.AgentServiceManager;
import com.telekom.cot.device.agent.service.AgentServiceShutdownHelper;

public class AppBootstrapFactory implements AppBootstrapSteps {

    private static final String EVENT_AGENT_STARTED_TYPE = "com_telekom_cot_device_agent_AgentStarted";
    private static final String EVENT_AGENT_STARTED_TEXT = "Agent was started successfully";
    private static final String EVENT_AGENT_STARTUP_CONDITION = "c8y_EventStartup";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AppBootstrapFactory.class);
    private final List<AgentService> startedServices = new ArrayList<>();  
    private final AppBootstrapServiceManager bootstrapServiceManager;
    private final AgentCredentialsManager credentialsManager;
    private final ConfigurationManager configurationManager;
    
    private AppBootstrapFactory(AgentServiceManager serviceManager, AgentCredentialsManager credentialsManager,
                    ConfigurationManager configurationManager) throws AppMainException {
        this.configurationManager = configurationManager;
        this.credentialsManager = credentialsManager;
        this.bootstrapServiceManager = new AppBootstrapServiceManagerImpl(serviceManager, credentialsManager, configurationManager);
        this.bootstrapServiceManager.loadAndInitializeAgentServices();
    }

    /**
     * Create a bootstrap factory with following managers:
     * <ul>
     * <li>AgentServiceManager - list of AgentService's</li>
     * </ul>
     * 
     * @param configurationManager
     * @param credentialsManager
     * @return
     * @throws AppMainException
     */
    public static AppBootstrapSteps getInstance(AgentServiceManager serviceManager, ConfigurationManager configurationManager, AgentCredentialsManager credentialsManager)
                    throws AppMainException {
        return new AppBootstrapFactory(serviceManager, credentialsManager, configurationManager);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public void checkConnectivity() throws AppMainException {
        LOGGER.debug("check connectivity to platform");
        ConnectivityChecker.checkPlatformConnectivity(configurationManager);
    }
    
    /* (non-Javadoc)
     * @see com.telekom.cot.device.agent.app.AppBootstrapIF#requestAndWriteDeviceCredentials()
     */
    @Override
    public void requestAndWriteDeviceCredentials() throws AppMainException {
        startService(DeviceCredentialsService.class);
        try {
            getService(DeviceCredentialsService.class).requestAndWriteDeviceCredentials();
        } catch (AbstractAgentException e) {
            LOGGER.error("can't request device credentials", e);
            throw new AppMainException("can't request device credentials", e);
        }
        stopService(DeviceCredentialsService.class);
    }

    /* (non-Javadoc)
     * @see com.telekom.cot.device.agent.app.AppBootstrapIF#credentialsAvailable()
     */
    @Override
    public boolean credentialsAvailable() {
        try {
            // try to get local credentials
            AgentCredentials agentCredentials = credentialsManager.getCredentials();
            LOGGER.info("found local device credentials: {}", agentCredentials);
            return Objects.nonNull(agentCredentials);
        } catch (AbstractAgentException e) {
            // no local credentials found
            LOGGER.info("found no local device credentials", e);
            return false;
        }
    }

    /* (non-Javadoc)
     * @see com.telekom.cot.device.agent.app.AppBootstrapIF#isDeviceRegistered()
     */
    @Override
    public boolean isDeviceRegistered() throws AppMainException {
        try {
            return getService(InventoryService.class).isDeviceRegistered();
        } catch (AbstractAgentException e) {
            LOGGER.error("can't get device registered status", e);
            throw new AppMainException("can't get device registered status", e);
        }
    }

    /* (non-Javadoc)
     * @see com.telekom.cot.device.agent.app.AppBootstrapIF#updateDevice()
     */
    @Override
    public void updateDevice() throws AppMainException {
        try {
            getService(InventoryService.class).updateDevice();
        } catch (AbstractAgentException e) {
            LOGGER.error("can't update device at platform", e);
            throw new AppMainException("can't update device at platform", e);
        }
    }

    /* (non-Javadoc)
     * @see com.telekom.cot.device.agent.app.AppBootstrapIF#sendEventAgentStarted()
     */
    @Override
    public void sendEventAgentStarted() throws AppMainException {
        try {
            EventService eventService = getService(EventService.class);
            eventService.createEvent(EVENT_AGENT_STARTED_TYPE, EVENT_AGENT_STARTED_TEXT, EVENT_AGENT_STARTUP_CONDITION);
        } catch (AbstractAgentException e) {
            LOGGER.error("can't send the AgentStarted event", e);
            throw new AppMainException("can't send the AgentStarted event", e);
        }
    }

    /* (non-Javadoc)
     * @see com.telekom.cot.device.agent.app.AppBootstrapIF#createAndRegisterDevice()
     */
    @Override
    public void createAndRegisterDevice() throws AppMainException {
        try {
            getService(InventoryService.class).createAndRegisterDevice();
        } catch (AbstractAgentException e) {
            LOGGER.error("can't create and register device at platform", e);
            throw new AppMainException("can't create and register device at platform", e);
        }
    }

    /* (non-Javadoc)
     * @see com.telekom.cot.device.agent.app.AppBootstrapIF#startService(java.lang.Class)
     */
    @Override
    public <T extends AgentService> void startService(Class<T> clazz) throws AppMainException {
        LOGGER.info("handle service start {}", clazz);
        try {
            startedServices.add(bootstrapServiceManager.getService(clazz));
            bootstrapServiceManager.startService(clazz);
        } catch (AppMainException exc) {
            LOGGER.error("can't start service " + clazz, exc);
            shutdownServices();
            throw exc;
        }
    }

    /* (non-Javadoc)
     * @see com.telekom.cot.device.agent.app.AppBootstrapIF#stop(com.telekom.cot.device.agent.service.AgentService)
     */
    @Override
    public <T extends AgentService> void stopService(Class<T> clazz) throws AppMainException {
        bootstrapServiceManager.stopService(clazz);
    }

    private <T extends AgentService> T getService(Class<T> clazz) throws AppMainException {
        return bootstrapServiceManager.getService(clazz);
    }

    private void shutdownServices() throws AppMainException {
        try {
            // get configuration
            CommonConfiguration cc = configurationManager.getConfiguration(CommonConfiguration.class);
            // reverse services (first in last out)
            Collections.reverse(startedServices);
            // shutdown services
            AgentServiceShutdownHelper.shutdownServices(startedServices, cc.getShutdownTimeout(), true);
        } catch (AbstractAgentException e) {
            throw new AppMainException("can't shutdown services", e);
        }
    }

    /**
     * Includes all about the {@code AgentServiceManager}, {@code AgentCredentialsManager} and {@code ConfigurationManager}.
     * The main functionality is:
     * <ul>
     *   <li>get a specific service</li>
     *   <li>start a specific service</li>
     *   <li>stop a specific service</li>
     * </ul> 
     *
     */
    protected static final class AppBootstrapServiceManagerImpl implements AppBootstrapServiceManager {

        private static final Logger LOGGER = LoggerFactory.getLogger(AppBootstrapServiceManagerImpl.class);
        private final AgentServiceManager serviceManager;
        private final AgentCredentialsManager credentialsManager;
        private final ConfigurationManager configurationManager;
        private Runtime runtime;

        public AppBootstrapServiceManagerImpl(final AgentServiceManager serviceManager,
                        final AgentCredentialsManager credentialsManager, final ConfigurationManager configurationManager) {
            this.serviceManager = serviceManager;
            this.credentialsManager = credentialsManager;
            this.configurationManager = configurationManager;
            runtime = Runtime.getRuntime();
        }

        @Override
        public void loadAndInitializeAgentServices() throws AppMainException {
            LOGGER.info("load and initialize agent services");
            try {
                serviceManager.loadAndInitServices(configurationManager, credentialsManager);
            } catch (AbstractAgentException e) {
                LOGGER.error("stop bootstrapping: can't initialize agent services", e);
                throw new AppMainException("stop bootstraping: can't initialize agent services", e);
            }
            try {
                runtime.addShutdownHook(new AppShutdown(serviceManager, configurationManager));
            } catch (AbstractAgentException e) {
                throw new AppMainException("can't add shutdown hook", e);
            }
        }

        @Override
        public <T extends AgentService> T getService(Class<T> clazz) throws AppMainException {
            // get and check service
            LOGGER.info("try to get get service {}", clazz);
            try {
                return serviceManager.getService(clazz);
            } catch (AbstractAgentException e) {
                LOGGER.error("can't get {} service", clazz, e);
                throw new AppMainException("can't get the service " + clazz, e);
            }
        }

        @Override
        public <T extends AgentService> void stopService(Class<T> clazz) throws AppMainException {
            // start service
            LOGGER.info("stop service {}", clazz);
            try {
                T service = this.getService(clazz);
                if (service.isStarted()) {
                    service.stop();
                }
            } catch (AbstractAgentException e) {
                LOGGER.error("can't stop {} service", clazz, e);
                throw new AppMainException("can't stop the service " + clazz, e);
            }
        }

        @Override
        public <T extends AgentService> void startService(Class<T> clazz) throws AppMainException {
            // start service
            LOGGER.info("start service {}", clazz);
            try {
                T service = this.getService(clazz);
                if (!service.isStarted()) {
                    service.start();
                }
            } catch (AbstractAgentException e) {
                LOGGER.error("can't start {} service", clazz, e);
                throw new AppMainException("can't start the service " + clazz, e);
            }
        }
        
    }
}
