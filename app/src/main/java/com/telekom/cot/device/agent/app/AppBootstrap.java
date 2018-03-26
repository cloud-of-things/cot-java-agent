package com.telekom.cot.device.agent.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.credentials.DeviceCredentialsService;
import com.telekom.cot.device.agent.inventory.InventoryService;
import com.telekom.cot.device.agent.operation.OperationService;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.sensor.EventService;
import com.telekom.cot.device.agent.sensor.SensorService;
import com.telekom.cot.device.agent.service.AgentService;
import com.telekom.cot.device.agent.service.AgentServiceManager;
import com.telekom.cot.device.agent.service.AgentServiceManagerImpl;
import com.telekom.cot.device.agent.service.configuration.AgentCredentials;
import com.telekom.cot.device.agent.service.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.service.configuration.AgentCredentialsManagerImpl;
import com.telekom.cot.device.agent.service.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.service.configuration.ConfigurationManagerImpl;
import com.telekom.cot.device.agent.system.SystemService;

public class AppBootstrap {

	private static final String EVENT_AGENT_STARTED_TYPE = "com_telekom_cot_device_agent_AgentStarted";
	private static final String EVENT_AGENT_STARTED_TEXT = "Agent was started successfully";
	private static final Logger LOGGER = LoggerFactory.getLogger(AppBootstrap.class);

	private ConfigurationManager configurationManager;
	private AgentCredentialsManager agentCredentialsManager;
	private AgentServiceManager agentServiceManager;

	private AppBootstrap(String configurationFile, String deviceCredentialsFile) {
		configurationManager = ConfigurationManagerImpl.getInstance(configurationFile);
		agentCredentialsManager = AgentCredentialsManagerImpl.getInstance(deviceCredentialsFile);
		agentServiceManager = AgentServiceManagerImpl.getInstance();
	}

	public static AppBootstrap getInstance(String configurationFile, String deviceCredentialsFile) {
		return new AppBootstrap(configurationFile, deviceCredentialsFile);
	}

	/**
	 * Start the main bootstrap steps.
	 * 
	 */
	public void start() throws AppMainException {
		LOGGER.info("start");

		// add shutdown hook
        try {
            Runtime.getRuntime().addShutdownHook(new AppShutdown(agentServiceManager, configurationManager));
        } catch (AbstractAgentException e) {
            throw new AppMainException("can't add shutdown hook", e);
        }
		

		loadAndInitializeAgentServices();

		startService(SystemService.class);

		// check if local device credentials are available
		DeviceCredentialsService deviceCredentialsService = startService(DeviceCredentialsService.class);
		if (!deviceCredentialsService.credentialsAvailable()) {
			// request device credentials at platform
			requestCredentials(deviceCredentialsService);
		}

		stop(deviceCredentialsService);
		startService(PlatformService.class);

		// check if device is already registered
		InventoryService inventoryService = startService(InventoryService.class);
		if (isDeviceRegistered(inventoryService)) {
			// update inventory if device is already registered
			updateDevice(inventoryService);
		} else {
			// create and register new device
			createAndRegisterDevice(inventoryService);
		}

		startService(OperationService.class);
		startService(SensorService.class);

		sendEventAgentStarted();

		LOGGER.info("finished bootstrap start");
	}

	/**
	 * Load and initialize all agent services.
	 * 
	 * @throws AppMainException
	 */
	private int loadAndInitializeAgentServices() throws AppMainException {
		LOGGER.info("load and initialize agent services");

		try {
			agentServiceManager.loadAndInitServices(configurationManager, agentCredentialsManager);
		} catch (AbstractAgentException e) {
			LOGGER.error("stop bootstrapping: can't initialize agent services", e);
			throw new AppMainException("stop bootstraping: can't initialize agent services", e);
		}

		return agentServiceManager.count();
	}

	private <T extends AgentService> T startService(Class<T> clazz) throws AppMainException {
		LOGGER.info("handle service start {}", clazz);
		T service = getService(clazz);
		start(service);
		return service;
	}

	private <T extends AgentService> T getService(Class<T> clazz) throws AppMainException {
		// get and check service
		LOGGER.info("try to get get service {}", clazz);
		try {
			return agentServiceManager.getService(clazz);
		} catch (AbstractAgentException e) {
			LOGGER.error("can't get {} service", clazz, e);
			throw new AppMainException("stop bootstraping: can't get the service " + clazz, e);
		}
	}

	private AgentService start(AgentService service) throws AppMainException {
		// start service
		LOGGER.info("start service {}", service.getClass());
		try {
			service.start();
			return service;
		} catch (AbstractAgentException e) {
			LOGGER.error("can't start {} service", service.getClass(), e);
			throw new AppMainException("stop bootstraping: can't start the service " + service.getClass(), e);
		}
	}

	private AgentService stop(AgentService service) throws AppMainException {
		// start service
		LOGGER.info("stop service {}", service.getClass());
		try {
			service.stop();
			return service;
		} catch (AbstractAgentException e) {
			LOGGER.error("can't correctly stop {} service", service.getClass(), e);
			throw new AppMainException("stop bootstraping: can't correctly stop the service " + service.getClass(), e);
		}
	}

	private void requestCredentials(DeviceCredentialsService deviceCredentialsService) throws AppMainException {
		// request device credentials
		AgentCredentials credentials;
		try {
			credentials = deviceCredentialsService.requestCredentials();
		} catch (AbstractAgentException e) {
			LOGGER.error("can't request device credentials", e);
			throw new AppMainException("can't request device credentials", e);
		}

		// write credentials to credentials file
		try {
			agentCredentialsManager.writeCredentials(credentials);
		} catch (AbstractAgentException e) {
			LOGGER.error("can't write requested device credentials to credentials file", e);
			throw new AppMainException("can't write requested device credentials to credentials file", e);
		}
	}

	private boolean isDeviceRegistered(InventoryService inventoryService) throws AppMainException {
		try {
			return inventoryService.isDeviceRegistered();
		} catch (AbstractAgentException e) {
			LOGGER.error("can't get device registered status", e);
			throw new AppMainException("can't get device registered status", e);
		}
	}

	private void updateDevice(InventoryService inventoryService) throws AppMainException {
		try {
			inventoryService.updateDevice();
		} catch (AbstractAgentException e) {
			LOGGER.error("can't update device at platform", e);
			throw new AppMainException("can't update device at platform", e);
		}
	}

	private void createAndRegisterDevice(InventoryService inventoryService) throws AppMainException {
		try {
			inventoryService.createAndRegisterDevice();
		} catch (AbstractAgentException e) {
			LOGGER.error("can't create and register device at platform", e);
			throw new AppMainException("can't create and register device at platform", e);
		}
	}

	private void sendEventAgentStarted() throws AppMainException {
		try {
			EventService eventService = getService(EventService.class);
			eventService.createEvent(EVENT_AGENT_STARTED_TYPE, EVENT_AGENT_STARTED_TEXT, null, null);
		} catch (AbstractAgentException e) {
			LOGGER.error("can't send the AgentStarted event", e);
			throw new AppMainException("can't send the AgentStarted event", e);
		}
	}
}
