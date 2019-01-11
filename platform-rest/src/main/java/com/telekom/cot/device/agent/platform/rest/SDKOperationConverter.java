package com.telekom.cot.device.agent.platform.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;
import com.telekom.m2m.cot.restsdk.util.ExtensibleObject;

/**
 * Class to map from and to sdk operation objects
 */
public class SDKOperationConverter {

    /**
     * private constructor to hide the implicit public one
     */
    private SDKOperationConverter() {
    }
    
    /**
     * convert the given REST-SDK operation to it's representational properties map ({@code Map<String, Object>})
     * @param operation REST-SDK operation to convert
     * @return a map representing the operation (it's properties), maybe empty
     */
    public static Map<String, Object> toPropertiesMap(Operation operation) {
        if (Objects.isNull(operation)) {
            return new HashMap<>();
        }
        
        // get operation attributes as map
        // filter all map values for instances of ExtensibleObject and convert them into Map<String,Object> values
        Map<String, Object> operationMap = operation.getAttributes();
        operationMap.entrySet().stream().filter(mapEntry -> ExtensibleObject.class.isInstance(mapEntry.getValue())).forEach(
                        mapEntry -> {
                            mapEntry.setValue(ExtensibleObject.class.cast(mapEntry.getValue()).getAttributes());
                        });
        
        // convert status
        OperationStatus status = operation.getStatus();
        if (Objects.nonNull(status)) {
            operationMap.put("status", status.name());
        }
        
        return operationMap;
    }

    /**
     * convert the given operation representation map into a REST-SDK operation
     * @param operationMap the map represents the operation (it's attributes)
     * @return a REST-SDK operation (maybe empty)
     */
    @SuppressWarnings("unchecked")
    public static Operation toOperation(Map<String, Object> operationMap) {
        Operation operation = new Operation();
        if (Objects.isNull(operationMap) || operationMap.isEmpty()) {
            return operation;
        }
        operation.setAttributes(operationMap);

        // filter all operation attribute values for instances of "Map<String,Object>" and
        // convert them into ExtensibleObject instances 
        operation.getAttributes().entrySet().stream().filter(mapEntry -> Map.class.isInstance(mapEntry.getValue())).forEach(
                        mapEntry -> {
                            Map<String, Object> map = null; 
                            try {
                                map = (Map<String, Object>)mapEntry.getValue();
                            } finally {
                            }

                            if (Objects.isNull(map)) {
                                return;
                            }
                            
                            ExtensibleObject extensibleObject = new ExtensibleObject();
                            extensibleObject.setAttributes(map);
                            operation.set(mapEntry.getKey(), extensibleObject);
                        });

        // convert status
        OperationStatus status = toSDKOperationStatus((String)operationMap.get("status"));
        operation.setStatus(status);
        return operation;
    }
    
	/**
	 * convert from operation status as string to REST-SDK operation status
	 */
	public static OperationStatus toSDKOperationStatus(String status) {
		return OperationStatus.valueOf(status);
	}
}
