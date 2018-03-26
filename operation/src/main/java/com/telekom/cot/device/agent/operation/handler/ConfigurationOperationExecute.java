package com.telekom.cot.device.agent.operation.handler;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentOperationHandlerException;
import com.telekom.cot.device.agent.service.configuration.Configuration;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;
import com.telekom.m2m.cot.restsdk.util.ExtensibleObject;

public class ConfigurationOperationExecute extends AbstractOperationExecute<Configuration> {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationOperationExecute.class);

    @Override
    public OperationStatus perform() throws AbstractAgentException {
        // c8y_Configuration as ExtensibleObject
        ExtensibleObject configuration;
        try {
            Object object = getOperation().get("c8y_Configuration");
            configuration = ExtensibleObject.class.cast(object);
        } catch (Exception e) {
            throw createExceptionAndLog(AgentOperationHandlerException.class, LOGGER, "c8y_Configuration as ExtensibleObject expected");
        }
         
        // get and check configuration content
        String configContent;
        try {
            configContent = (String)configuration.get("config");
        } catch (Exception e) {
            throw createExceptionAndLog(AgentOperationHandlerException.class, LOGGER, "config as String expected");
        }
        assertNotEmpty(configContent, AgentOperationHandlerException.class, LOGGER, "config value expected (is null or empty)");

        // callback
        assertIsTrue(isCallback(), AgentOperationHandlerException.class, LOGGER, "config callback expected");
        getCallback(String.class).finished(configContent);
        return OperationStatus.SUCCESSFUL;
    }

}
