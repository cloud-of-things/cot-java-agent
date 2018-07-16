package com.telekom.cot.device.agent.app;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.alarm.AlarmService;
import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentShutdownException;
import com.telekom.cot.device.agent.event.EventService;
import com.telekom.cot.device.agent.inventory.InventoryService;
import com.telekom.cot.device.agent.operation.OperationService;
import com.telekom.cot.device.agent.operation.OperationServiceConfiguration;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.sensor.SensorService;
import com.telekom.cot.device.agent.service.AgentServiceProvider;
import com.telekom.cot.device.agent.service.AgentServiceShutdownHelper;
import com.telekom.cot.device.agent.system.SystemService;

public class AppShutdown extends Thread {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppShutdown.class);

	private AgentServiceShutdownHelper shutdownHelper;
	private ConfigurationManager configurationManager;
	private CommonConfiguration commonConfiguration;

	public AppShutdown(AgentServiceProvider agentServiceProvider, ConfigurationManager configurationManager)
			throws AbstractAgentException {
		assertNotNull(agentServiceProvider, AgentShutdownException.class, LOGGER, "no agent service provider given");
		assertNotNull(configurationManager, AgentShutdownException.class, LOGGER, "no configuration manager given");

		this.configurationManager = configurationManager;

		shutdownHelper = new AgentServiceShutdownHelper(agentServiceProvider);
		commonConfiguration = configurationManager.getConfiguration(CommonConfiguration.class);
	}

	@Override
	public void run() {
		LOGGER.debug("shutdown agent...");

		// shut down all services
		shutDownSensorService();
		shutDownAlarmService();
		shutDownEventService();
		shutDownOperationService();
		shutDownInventoryService();
		shutDownPlatformService();
		shutDownSystemService();

		LOGGER.info("shutdown completed");
	}

	/**
	 * shut down sensor service and all sensor device services
	 */
	private void shutDownSensorService() {
		LOGGER.debug("stop the sensor service and all sensor device services");

		try {
			shutdownHelper.shutdownService(SensorService.class, commonConfiguration.getShutdownTimeout(), true);
			LOGGER.info("shut down sensor service and all sensor device services successfully");
		} catch (AbstractAgentException e) {
			LOGGER.error("can't shut down sensor service and all sensor device services", e);
		}
	}
	
	/**
	 * shut down alarm service
	 */
	private void shutDownAlarmService() {
		LOGGER.debug("shut down alarm service");

		try {
			shutdownHelper.shutdownService(AlarmService.class, commonConfiguration.getShutdownTimeout(), true);
			LOGGER.info("shut down event alarm successfully");
		} catch (AbstractAgentException e) {
			LOGGER.error("can't shut down alarm service", e);
		}
	}

	/**
	 * shut down event service
	 */
	private void shutDownEventService() {
		LOGGER.debug("shut down event service");

		try {
			shutdownHelper.shutdownService(EventService.class, commonConfiguration.getShutdownTimeout(), true);
			LOGGER.info("shut down event service successfully");
		} catch (AbstractAgentException e) {
			LOGGER.error("can't shut down event service", e);
		}
	}

	/**
	 * shut down operation service and all operation handlers
	 */
	private void shutDownOperationService() {
		LOGGER.debug("shut down the operation service and all operation handlers");

		// try to get shutdown timeout from operation service configuration
		int timeout = commonConfiguration.getShutdownTimeout();
		try {
			timeout = configurationManager.getConfiguration(OperationServiceConfiguration.class).getShutdownTimeout();
		} catch (Exception e) {
			LOGGER.warn("can't get shutdown timeout from operation service configuration, use common timeout", e);
		}

		try {
			shutdownHelper.shutdownService(OperationService.class, timeout, true);
			LOGGER.info("shut down operation service and all operation handlers successfully");
		} catch (AbstractAgentException e) {
			LOGGER.error("can't shut down operation service and all operation handlers", e);
		}
	}

	/**
	 * shut down inventory service
	 */
	private void shutDownInventoryService() {
		LOGGER.debug("shut down inventory service");

		try {
			shutdownHelper.shutdownService(InventoryService.class, commonConfiguration.getShutdownTimeout(), true);
			LOGGER.info("shut down inventory service successfully");
		} catch (AbstractAgentException e) {
			LOGGER.error("can't shut down inventory service", e);
		}
	}

	/**
	 * shut down platform service
	 */
	private void shutDownPlatformService() {
		LOGGER.debug("shut down platform service");

		try {
			shutdownHelper.shutdownService(PlatformService.class, commonConfiguration.getShutdownTimeout(), true);
			LOGGER.info("shut down platform service successfully");
		} catch (AbstractAgentException e) {
			LOGGER.error("can't shut down platform service", e);
		}
	}

	/**
	 * shut down system service
	 */
	private void shutDownSystemService() {
		LOGGER.debug("shut down system service");

		try {
			shutdownHelper.shutdownService(SystemService.class, commonConfiguration.getShutdownTimeout(), true);
			LOGGER.info("shut down system service successfully");
		} catch (AbstractAgentException e) {
			LOGGER.error("can't shut down system service", e);
		}
	}
}
