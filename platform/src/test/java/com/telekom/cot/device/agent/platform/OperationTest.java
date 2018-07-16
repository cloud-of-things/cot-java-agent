package com.telekom.cot.device.agent.platform;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.telekom.cot.device.agent.platform.objects.Operation;
import com.telekom.cot.device.agent.platform.objects.OperationStatus;


public class OperationTest {
    
    private HashMap<String, Object> testProperties = new HashMap<>();
    
    private static final String id = "testId";
    private static final String deviceId = "testDeviceId";
    private static final String deliveryType = "testDeliveryType";
    
    Operation testOperation = new Operation(id);
    
    @Before
    public void setUp() {
        
        //setup hash map in Operation named testOperation
        testOperation.setId(id);
        testOperation.setDeviceId(deviceId);
        testOperation.setDeliveryType(deliveryType);
        
        //setup hash map in OperationTest
        testProperties.put("id", "testId");
        testProperties.put("deviceId", deviceId);
        testProperties.put("deliveryType", deliveryType);
        testProperties.put("status", OperationStatus.ACCEPTED);
        
        testOperation.setProperties(testProperties);
    }
    
    @Test
    public void testGetterAndSetter() {
        //add OperationStatus ACCEPTED
        testOperation.setStatus(OperationStatus.ACCEPTED);
        
        assertEquals(id, testOperation.getId() );
        assertEquals(deviceId, testOperation.getDeviceId() );
        assertEquals(deliveryType, testOperation.getDeliveryType() );
        assertEquals(testProperties.get("status"), testOperation.getStatus() ); 
        assertEquals(testProperties, testOperation.getProperties() );
        
    }
    
    @Test
    public void testRemoveProperty() {
        testOperation.removeProperty("status");
        assertNull(testOperation.getProperty("status", OperationStatus.class) );
    }
    
    @Test
    public void testPropertyReturnNull() {
        assertNull(testOperation.getProperty(null, null) );
    }
    
}
