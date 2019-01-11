package com.telekom.cot.device.agent.operation.operations;

import java.util.Objects;

import com.telekom.cot.device.agent.platform.objects.operation.Operation;
import com.telekom.cot.device.agent.platform.objects.operation.OperationAttributes;

@OperationAttributes(name = "c8y_Configuration")
public class ConfigurationUpdateOperation extends Operation {
    
    /**
     * default constructor
     */
    public ConfigurationUpdateOperation() {
        super();
        setConfiguration("");
    }
    
    /**
     * get the configuration to update as string
     * @return the configuration to update (maybe empty)
     */
    public String getConfiguration() {
        return getPropertyValueFromFragment("config", "", String.class);
    }
    
    /**
     * set the configuration to update as string
     * @param config configuration to updateg
     */
    public void setConfiguration(String config) {
        setPropertyValueAtFragment("config", Objects.nonNull(config) ? config : "");
    }
}
