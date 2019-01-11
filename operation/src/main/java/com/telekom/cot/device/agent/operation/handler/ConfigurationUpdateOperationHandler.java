package com.telekom.cot.device.agent.operation.handler;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.*;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.OperationHandlerServiceException;
import com.telekom.cot.device.agent.common.injection.Inject;
import com.telekom.cot.device.agent.inventory.InventoryService;
import com.telekom.cot.device.agent.operation.operations.ConfigurationUpdateOperation;
import com.telekom.cot.device.agent.platform.objects.operation.Operation.OperationStatus;
import com.telekom.cot.device.agent.service.AbstractAgentService;
import com.telekom.cot.device.agent.system.SystemService;
import com.telekom.cot.device.agent.system.properties.ConfigurationProperties;


public class ConfigurationUpdateOperationHandler extends AbstractAgentService implements OperationHandlerService<ConfigurationUpdateOperation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationUpdateOperationHandler.class);
    
    @Inject
    private ConfigurationManager configurationManager;
    
    @Inject
    private SystemService systemService;
    
    @Inject
    private InventoryService inventoryService;

    @Override
    public Class<ConfigurationUpdateOperation> getSupportedOperationType() {
        return ConfigurationUpdateOperation.class;
    }

    @Override
    public OperationStatus execute(ConfigurationUpdateOperation operation) throws AbstractAgentException {
        assertNotNull(operation, OperationHandlerServiceException.class, LOGGER, "no ConfigurationUpdateOperation given to execute");
        LOGGER.debug("execute ConfigurationUpdateOperation {}", operation.getProperties());

        String configuration = operation.getConfiguration();
        assertNotEmpty(configuration, OperationHandlerServiceException.class, LOGGER, "config value expected (is null or empty)");

        LOGGER.info("update agent configuration {}", configuration);
        configurationManager.updateConfiguration(configuration);

        // update the managedObjects c8y_Configuration Fragment
        ConfigurationProperties configurationProperties = systemService.getProperties(ConfigurationProperties.class);
        if (Objects.nonNull(configurationProperties)) {
            configurationProperties.setConfig(configuration);
        }
        
        inventoryService.updateDevice();
        return OperationStatus.SUCCESSFUL;
    }
}
