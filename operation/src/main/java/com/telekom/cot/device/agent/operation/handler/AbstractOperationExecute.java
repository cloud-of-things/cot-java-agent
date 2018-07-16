package com.telekom.cot.device.agent.operation.handler;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

import com.telekom.cot.device.agent.common.configuration.Configuration;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentOperationHandlerException;
import com.telekom.cot.device.agent.platform.objects.Operation;

public abstract class AbstractOperationExecute<C extends Configuration> implements OperationExecute<C> {

    private Operation operation;
    private Configuration configuration;
    private Map<String, Object> params;
    private OperationExecuteCallback<?> callback;

    @Override
    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void setParameter(Map<String, Object> params) {
        this.params = params;
    }

    @Override
    public void setCallback(OperationExecuteCallback<?> callback) {
        this.callback = callback;
    }

    public boolean isCallback() {
        return Objects.nonNull(callback);
    }

    public <B> OperationExecuteCallback<B> getCallback(Class<B> clazz) {
        return new OperationExecuteCallbackWrapper<B>(clazz, callback);
    }

    @SuppressWarnings("unchecked")
	public C getConfiguration() {
        return (C) configuration;
    }

    public Operation getOperation() {
        return operation;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public Integer getParamValueAsInteger(String param) {
        if (Objects.nonNull(params)) {
            Object value = params.get(param);
            if (Objects.nonNull(value) && Integer.class.isInstance(value)) {
                return Integer.class.cast(value);
            }
        }
        return null;
    }

    public String getParamValueAsString(String param) {
        if (Objects.nonNull(params)) {
            Object value = params.get(param);
            if (Objects.nonNull(value) && String.class.isInstance(value)) {
                return String.class.cast(value);
            }
        }
        return null;
    }

    private static class OperationExecuteCallbackWrapper<B> implements OperationExecuteCallback<B> {

        private Class<B> clazz;
        private OperationExecuteCallback<B> callback;

        @SuppressWarnings("unchecked")
        public OperationExecuteCallbackWrapper(Class<B> clazz, OperationExecuteCallback<?> callback) {
            this.clazz = clazz;
            this.callback = (OperationExecuteCallback<B>) callback;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void finished(Object content) throws AbstractAgentException {
            if (isCorrect()) {
                callback.finished((B) content);
            } else {
                throw new AgentOperationHandlerException("is not the correct content type");
            }
        }

        private boolean isCorrect() {
            Type[] genericInterfaces = callback.getClass().getGenericInterfaces();
            // search the correct interface
            for (Type genericInterface : genericInterfaces) {
                ParameterizedType genericInterfaceParamType = (ParameterizedType) genericInterface;
                // check interface
                if (isCorrectInterface(genericInterfaceParamType)
                                && isCorrectGenericType(genericInterfaceParamType.getActualTypeArguments())) {
                    return true;
                }
            }
            return false;
        }

        private boolean isCorrectInterface(ParameterizedType genericInterfaceParamType) {
            return OperationExecuteCallback.class.getName().equals(genericInterfaceParamType.getRawType().getTypeName());
        }

        private boolean isCorrectGenericType(Type[] typeArguments) {
            boolean b = Objects.nonNull(typeArguments);
            b = b && typeArguments.length == 1;
            b = b && clazz.getName().equals(typeArguments[0].getTypeName());
            return b;
        }
    }

}
