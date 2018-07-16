package com.telekom.cot.device.agent.platform.rest.util;

import java.util.HashMap;
import java.util.Map;

import com.telekom.m2m.cot.restsdk.util.ExtensibleObject;

/**
 * Class to map from and to sdk operation objects
 */
public class OperationMapper {
	/**
	 * From REST-SDK to internal operation
	 * @param sdkOperation 
	 * @return Operation
	 */
	public static com.telekom.cot.device.agent.platform.objects.Operation fromSDKOperation(com.telekom.m2m.cot.restsdk.devicecontrol.Operation sdkOperation) {
	    // create new operation
	    com.telekom.cot.device.agent.platform.objects.Operation operation = new com.telekom.cot.device.agent.platform.objects.Operation();
		
        // transform ExtensibleObject attributes into Map<String,Object> properties
	    // set other properties unchanged
	    Map<String, Object> sdkOperationAttributes = sdkOperation.getAttributes();
        for(String attributeName : sdkOperationAttributes.keySet()) {
            Object attribute = sdkOperationAttributes.get(attributeName);
            if (ExtensibleObject.class.isInstance(attribute)) {
                operation.setProperty(attributeName, ExtensibleObject.class.cast(attribute).getAttributes()); 
            } else {
                operation.setProperty(attributeName, attribute);
            }
        }
		
		// transform operation status object 
		operation.setStatus(fromSDKOperationStatus(sdkOperation.getStatus()));
		return operation;
	}
	
	/**
	 * From internal operation to REST-SDK
	 * @param operation
	 * @return Operation
	 */
	@SuppressWarnings("unchecked")
    public static com.telekom.m2m.cot.restsdk.devicecontrol.Operation toSDKOperation(com.telekom.cot.device.agent.platform.objects.Operation operation) {
	    // create new REST SDK operation and set attributes
	    com.telekom.m2m.cot.restsdk.devicecontrol.Operation sdkOperation = new com.telekom.m2m.cot.restsdk.devicecontrol.Operation();

        // transform Map<String,Object> properties into ExtensibleObject attributes
        // set other attributes unchanged
        Class<?> mapType = new HashMap<String, Object>().getClass();
        Map<String, Object> operationProperties = operation.getProperties();
        for(String propertyName : operationProperties.keySet()) {
            Object property = operationProperties.get(propertyName);
            if (mapType.isInstance(property)) {
                ExtensibleObject extensibleObject = new ExtensibleObject();
                extensibleObject.setAttributes((Map<String,Object>)property);
                sdkOperation.set(propertyName, extensibleObject);
            } else {
                sdkOperation.set(propertyName, property);
            }
        }
		
        // transform operation status object 
		sdkOperation.setStatus(toSDKOperationStatus(operation.getStatus()));
		return sdkOperation;
	}
	
	/**
	 * From REST-SDK operation status to internal operation status
	 * @param sdkOperationStatus
	 * @return OperationStatus
	 */
	public static com.telekom.cot.device.agent.platform.objects.OperationStatus fromSDKOperationStatus(com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus sdkOperationStatus) {
		return com.telekom.cot.device.agent.platform.objects.OperationStatus.valueOf(sdkOperationStatus.name());
	}
	
	/**
	 * From internal operation status to REST-SDK operation status
	 * @param operationStatus
	 * @return OperationStatus
	 */
	public static com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus toSDKOperationStatus(com.telekom.cot.device.agent.platform.objects.OperationStatus operationStatus) {
		return com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus.valueOf(operationStatus.name());
	}
}
