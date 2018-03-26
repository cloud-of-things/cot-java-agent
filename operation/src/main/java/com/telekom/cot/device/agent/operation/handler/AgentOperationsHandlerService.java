package com.telekom.cot.device.agent.operation.handler;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.telekom.cot.device.agent.inventory.InventoryService;
import com.telekom.cot.device.agent.operation.softwareupdate.SoftwareUpdateExecute;
import com.telekom.cot.device.agent.operation.softwareupdate.SoftwareUpdateExecuteCallback;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.system.SystemService;
import com.telekom.cot.device.agent.system.properties.ConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentOperationHandlerException;
import com.telekom.cot.device.agent.common.util.AssertionUtil;
import com.telekom.cot.device.agent.service.AbstractAgentService;
import com.telekom.cot.device.agent.service.configuration.Configuration;
import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;

public class AgentOperationsHandlerService extends AbstractAgentService implements OperationsHandlerService {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentOperationsHandlerService.class);

    private AgentOperationsHandlerConfiguration config;

    /**
     * The operation type is the key to executor implementation.
     */
    public enum OperationType {

        C8Y_TEST_OPERATION("c8y_TestOperation"),
        C8Y_CONFIGURATION("c8y_Configuration"),
        C8Y_SOFTWARELIST("c8y_SoftwareList");

        private String attribute;

        private OperationType(String attribute) {
            this.attribute = attribute;
        }

        public String getAttribute() {
            return attribute;
        }

        public static OperationType findByAttributes(Set<String> attributes) {
            return Arrays.asList(OperationType.values()).stream()
                    .filter(o -> attributes.contains(o.getAttribute()))
                    .findFirst()
                    .get();
        }

        public static List<String> attributes() {
            return Arrays.asList(OperationType.values()).stream()
                    .map(OperationType::getAttribute)
                    .collect(Collectors.toList());
        }
    }
    
    @Override
    public void start() throws AbstractAgentException {
        super.start();
        config = getConfigurationManager().getConfiguration(AgentOperationsHandlerConfiguration.class);
    }

    @Override
    public OperationStatus execute(Operation operation) throws AbstractAgentException {
        LOGGER.info("start operation executing");
        // check the operation
        AssertionUtil.assertNotNull(operation, AgentOperationHandlerException.class, LOGGER, "operation is required");
        AssertionUtil.assertNotEmpty(operation.getAttributes(), AgentOperationHandlerException.class, LOGGER,
                "operation has no attributes to identify the executor");
        // exist type
        OperationType operationType = OperationType.findByAttributes(operation.getAttributes().keySet());
        AssertionUtil.assertNotNull(operationType, AgentOperationHandlerException.class, LOGGER,
                "did not found the operation type");
        // execute
        switch (operationType) {
        case C8Y_TEST_OPERATION:
            return handleDemoOperation(operation);
            
        case C8Y_CONFIGURATION:
            return handleConfigurationOperation(operation);

        case C8Y_SOFTWARELIST:
            return handleSoftwarelistOperation(operation, System.getProperty("user.dir"), Runtime.getRuntime());

        default:
            LOGGER.error("could not execute operation");
            throw new AgentOperationHandlerException("did not found operation executor");
        }
    }

    @Override
    public String[] getSupportedOperations() {
        return OperationType.attributes().toArray(new String[] {});
    }

    private OperationStatus handleDemoOperation(Operation operation) throws AbstractAgentException {
        OperationExecute<? extends Configuration> execute = OperationExecuteBuilder.create(operation)
                        .setExecutorClass(TestOperationExecute.class)
                        .setConfiguration(config.getTestOperation()).build();
        
        return execute.perform();
    }

    private OperationStatus handleConfigurationOperation(Operation operation) throws AbstractAgentException {
        OperationExecute<? extends Configuration> execute = OperationExecuteBuilder.create(operation)
                        .setExecutorClass(ConfigurationOperationExecute.class)
                        .setCallback(new OperationExecuteCallback<String>() {
                            public void finished(String configuration) throws AbstractAgentException {
                                LOGGER.info("update agent configuration {}", configuration);
                                getConfigurationManager().updateConfiguration(configuration);

                                // update the managedObjects c8y_Configuration Fragment
                                SystemService systemService = getService(SystemService.class);
                                ConfigurationProperties configurationProperties = systemService.getProperties(ConfigurationProperties.class);
                                if (Objects.nonNull(configurationProperties)) {
                                    configurationProperties.setConfig(configuration);
                                }
                                
                                getService(InventoryService.class).updateDevice();
                            }
                        })
                        .build();
        
        return execute.perform();
    }

    protected OperationStatus handleSoftwarelistOperation(Operation operation,String agentWorkingDir, Runtime runtime) throws AbstractAgentException {
        OperationExecute<? extends Configuration> execute = OperationExecuteBuilder.create(operation)
                        .setExecutorClass(SoftwareUpdateExecute.class)
                        .setConfiguration(config.getSoftwareUpdate())
                        .setCallback(new SoftwareUpdateExecuteCallback(getService(PlatformService.class), config.getSoftwareUpdate().getChecksumAlgorithm()))   
                        .build();

        return execute.perform();
    }
}
