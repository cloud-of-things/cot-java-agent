package com.telekom.cot.device.agent.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.alarm.AlarmService;
import com.telekom.cot.device.agent.common.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.device.DeviceService;
import com.telekom.cot.device.agent.event.EventService;
import com.telekom.cot.device.agent.inventory.InventoryService;
import com.telekom.cot.device.agent.measurement.MeasurementService;
import com.telekom.cot.device.agent.operation.OperationService;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.service.AgentServiceManager;
import com.telekom.cot.device.agent.system.SystemService;

public final class AppBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppBootstrap.class);
    private AppBootstrapSteps steps;

    private AppBootstrap(AgentServiceManager serviceManager, ConfigurationManager configurationManager, AgentCredentialsManager credentialsManager) throws AppMainException {
        steps = AppBootstrapFactory.getInstance(serviceManager, configurationManager, credentialsManager);
    }

    public static AppBootstrap getInstance(AgentServiceManager serviceManager, ConfigurationManager configurationManager, AgentCredentialsManager credentialsManager) throws AppMainException {
        return new AppBootstrap(serviceManager, configurationManager, credentialsManager);
    }

    /**
     * Start the main bootstrap steps.
     */
    public void start() throws AppMainException {
        LOGGER.info("start");

        // check platform connectivity
        steps.checkConnectivity();
        
        steps.startService(SystemService.class);

        // check if local device credentials are available
        if (!steps.credentialsAvailable()) {
            // request device credentials at platform
            steps.requestAndWriteDeviceCredentials();
        }
        
        steps.startService(PlatformService.class);

        // check if device is already registered
        steps.startService(InventoryService.class);
        if (steps.isDeviceRegistered()) {
            // update inventory if device is already registered
            steps.updateDevice();
        } else {
            // create and register new device
            steps.createAndRegisterDevice();
        }

        steps.startService(OperationService.class);
        steps.startService(EventService.class);
        steps.startService(AlarmService.class);
        steps.startService(DeviceService.class);
        steps.startService(MeasurementService.class);

        steps.sendEventAgentStarted();

        LOGGER.info("finished bootstrap start");
    }

}
