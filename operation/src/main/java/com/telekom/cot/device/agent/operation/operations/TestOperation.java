package com.telekom.cot.device.agent.operation.operations;

import java.util.Arrays;

import com.telekom.cot.device.agent.platform.objects.operation.Operation;
import com.telekom.cot.device.agent.platform.objects.operation.OperationAttributes;

@OperationAttributes(name = "c8y_TestOperation")
public class TestOperation extends Operation {

    public GivenStatus getGivenStatus() {
        Object givenStatus = this.getPropertyValueFromFragment("givenStatus", null, Object.class);
        if (GivenStatus.class.isInstance(givenStatus)) {
            return GivenStatus.class.cast(givenStatus);
        }
        
        if (String.class.isInstance(givenStatus)) {
            return GivenStatus.find(String.class.cast(givenStatus));
        }
        
        return null;
    }

    public void setGivenStatus(GivenStatus status) {
        setPropertyValueAtFragment("givenStatus", status);
    }
    
    public void setGivenStatus(String status) {
        setGivenStatus(GivenStatus.find(status));
    }
    
    public enum GivenStatus {
        GIVEN_SUCCESSFUL,
        GIVEN_FAILED_BY_STATUS,
        GIVEN_FAILED_BY_EXCEPTION;
        
        public static GivenStatus find(String status) {
            return Arrays.asList(GivenStatus.values()).stream()
                            .filter(value -> String.valueOf(value).equals(status))
                            .findFirst()
                            .orElse(null);
        }
    }
}
