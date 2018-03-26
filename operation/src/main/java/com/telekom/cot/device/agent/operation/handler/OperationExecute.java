package com.telekom.cot.device.agent.operation.handler;

import java.util.Map;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.service.configuration.Configuration;
import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;

public interface OperationExecute<C extends Configuration> {

    public void setOperation(Operation operation);

    public void setParameter(Map<String, Object> params);

    public void setConfiguration(Configuration config);

    public void setCallback(OperationExecuteCallback<?> callback);

    public C getConfiguration();

    public OperationStatus perform() throws AbstractAgentException;

}
