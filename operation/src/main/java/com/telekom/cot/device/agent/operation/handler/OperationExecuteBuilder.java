package com.telekom.cot.device.agent.operation.handler;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.assertNotNull;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.configuration.Configuration;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentOperationHandlerException;
import com.telekom.cot.device.agent.platform.objects.Operation;

public class OperationExecuteBuilder {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationExecuteBuilder.class);

    private Operation operation;
    private Class<? extends OperationExecute<? extends Configuration>> operationExecutClass;
    private Map<String, Object> params;
    private Configuration configuration;
    private OperationExecuteCallback<?> callback;

    private OperationExecuteBuilder(Operation operation) {
        this.operation = operation;
    }

    public static OperationExecuteBuilder create(Operation operation) {
        return new OperationExecuteBuilder(operation);
    }

    public <E extends OperationExecute<?>> OperationExecuteBuilder setExecutorClass(Class<E> operationExecutClass) {
        this.operationExecutClass = operationExecutClass;
        return this;
    }

    public OperationExecuteBuilder setParameters(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public OperationExecuteBuilder setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    public OperationExecuteBuilder addParameter(String key, Object value) {
        if (Objects.isNull(params)) {
            params = new HashMap<>();
        }
        params.put(key, value);
        return this;
    }

    public OperationExecuteBuilder setCallback(OperationExecuteCallback<?> callback) {
        this.callback = callback;
        return this;
    }

    public <T extends Configuration> OperationExecute<T> build() throws AbstractAgentException {
        // check
        assertNotNull(operationExecutClass, AgentOperationHandlerException.class, LOGGER, "operation execut class is null");
        assertNotNull(operation, AgentOperationHandlerException.class, LOGGER, "operation is null");
        // create instance
        OperationExecute<T> operationExecut = createOperationExecute();
        // set operation
        operationExecut.setOperation(operation);
        // set configuration
        if (Objects.nonNull(callback)) {
            operationExecut.setCallback(callback);
        }
        // set configuration
        if (Objects.nonNull(configuration)) {
            operationExecut.setConfiguration(configuration);
        }
        // set params
        if (Objects.nonNull(params)) {
            operationExecut.setParameter(params);
        }
        return operationExecut;
    }

    @SuppressWarnings("unchecked")
    private <T extends Configuration> OperationExecute<T> createOperationExecute() throws AbstractAgentException {
        try {
            // get constructor
            Constructor<? extends OperationExecute<? extends Configuration>> defaultConstructor =
                    operationExecutClass.getConstructor(new Class[] {});
            // return
            return (OperationExecute<T>) defaultConstructor.newInstance(new Object[] {});
        } catch (Exception e) {
            LOGGER.error("default constructor does not exist in " + operationExecutClass.getName());
            throw new AgentOperationHandlerException("can't create instance of " + operationExecutClass.getName(), e);
        }
    }

}
