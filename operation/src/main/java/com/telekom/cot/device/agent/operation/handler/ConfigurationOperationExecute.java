package com.telekom.cot.device.agent.operation.handler;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.assertIsTrue;
import static com.telekom.cot.device.agent.common.util.AssertionUtil.assertNotEmpty;
import static com.telekom.cot.device.agent.common.util.AssertionUtil.createExceptionAndLog;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.configuration.Configuration;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentOperationHandlerException;
import com.telekom.cot.device.agent.platform.objects.OperationStatus;

public class ConfigurationOperationExecute extends AbstractOperationExecute<Configuration> {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationOperationExecute.class);

    @SuppressWarnings("unchecked")
    @Override
    public OperationStatus perform() throws AbstractAgentException {
        // get c8y_Configuration as Map
        Map<String, Object> configuration = getOperation().getProperty("c8y_Configuration", new HashMap<String, Object>().getClass());
        if (Objects.isNull(configuration)) {
            throw createExceptionAndLog(AgentOperationHandlerException.class, LOGGER, "Did not find element c8y_Configuration");
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
