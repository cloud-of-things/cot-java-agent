package com.telekom.cot.device.agent.platform.rest;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;
import com.telekom.m2m.cot.restsdk.util.ExtensibleObject;


public class SDKOperationConverterTest {

    private final static String OPERATION_ID = "192837";
    private final static String DEVICE_ID = "testDevice123";
    private final static OperationStatus OPERATION_STATUS = OperationStatus.EXECUTING;
    private final static String OPERATION_NAME = "c8y_Test";
    private final static String OPERATION_PROPERTY = "testProperty";
    private final static String OPERATION_PROPERTY_VALUE = "testValue0815";
    
    private Operation operation;
    private Map<String, Object> operationMap;
    
    @Before
    public void setUp() {
        ExtensibleObject operationFragment = new ExtensibleObject();
        operationFragment.set(OPERATION_PROPERTY, OPERATION_PROPERTY_VALUE);
        
        operation = new Operation(OPERATION_ID);
        operation.setDeviceId(DEVICE_ID);
        operation.setStatus(OPERATION_STATUS);
        operation.set(OPERATION_NAME, operationFragment);

        Map<String, Object> fragmentMap = new HashMap<>();
        fragmentMap.put(OPERATION_PROPERTY, OPERATION_PROPERTY_VALUE);
        
        operationMap = new HashMap<>();
        operationMap.put("id", OPERATION_ID);
        operationMap.put("deviceId", DEVICE_ID);
        operationMap.put("status", OPERATION_STATUS.name());
        operationMap.put(OPERATION_NAME, fragmentMap);
    }
    
    @Test
    public void testToPropertiesMapNull() {
        Map<String, Object> operationMap = SDKOperationConverter.toPropertiesMap(null);
        assertNotNull(operationMap);
        assertTrue(operationMap.isEmpty());
    }

    @Test
    public void testToPropertiesMapEmptyOperation() {
        Map<String, Object> operationMap = SDKOperationConverter.toPropertiesMap(new Operation());
        assertNotNull(operationMap);
        assertTrue(operationMap.isEmpty());
    }

    @Test
    public void testToPropertiesMapOperationWithoutExtensibleObject() {
        operation.set(OPERATION_NAME, "test");

        Map<String, Object> operationMap = SDKOperationConverter.toPropertiesMap(operation);
        
        assertNotNull(operationMap);
        assertEquals(OPERATION_ID, operationMap.get("id"));
        assertEquals(DEVICE_ID, operationMap.get("deviceId"));
        assertEquals(OPERATION_STATUS.toString(), operationMap.get("status"));
        assertEquals("test", operationMap.get(OPERATION_NAME));
    }

    @Test
    public void testToPropertiesMap() {
        Map<String, Object> operationMap = SDKOperationConverter.toPropertiesMap(operation);
        @SuppressWarnings("unchecked")
        Map<String, Object> operationFragment = (Map<String, Object>) operationMap.get(OPERATION_NAME);

        assertNotNull(operationMap);
        assertEquals(OPERATION_ID, operationMap.get("id"));
        assertEquals(DEVICE_ID, operationMap.get("deviceId"));
        assertEquals(OPERATION_STATUS.toString(), operationMap.get("status"));
        assertEquals(OPERATION_PROPERTY_VALUE, operationFragment.get(OPERATION_PROPERTY));
    }

    @Test
    public void testToOperationNull() {
        Operation operation = SDKOperationConverter.toOperation(null);
        assertNotNull(operation);
        assertTrue(operation.getAttributes().isEmpty());
    }

    @Test
    public void testToOperationEmptyMap() {
        Operation operation = SDKOperationConverter.toOperation(new HashMap<>());
        assertNotNull(operation);
        assertTrue(operation.getAttributes().isEmpty());
    }

    @Test
    public void testToOperationWithoutExtensibleObject() {
        operationMap.put(OPERATION_NAME, "test");
        
        Operation operation = SDKOperationConverter.toOperation(operationMap);
        assertEquals(OPERATION_ID, operation.getId());
        assertEquals(DEVICE_ID, operation.getDeviceId());
        assertEquals(OPERATION_STATUS, operation.get("status"));
        assertEquals("test", operation.get(OPERATION_NAME));
    }

    @Test
    public void testToOperation() {
        Operation operation = SDKOperationConverter.toOperation(operationMap);
        ExtensibleObject fragment = (ExtensibleObject) operation.get(OPERATION_NAME);
        
        assertEquals(OPERATION_ID, operation.getId());
        assertEquals(DEVICE_ID, operation.getDeviceId());
        assertEquals(OPERATION_STATUS, operation.get("status"));
        assertEquals(OPERATION_PROPERTY_VALUE, fragment.getAttributes().get(OPERATION_PROPERTY));
    }
}
