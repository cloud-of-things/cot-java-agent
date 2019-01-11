package com.telekom.cot.device.agent.operation.operations;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.telekom.cot.device.agent.operation.operations.ConfigurationUpdateOperation;
import com.telekom.cot.device.agent.platform.objects.operation.Operation.OperationStatus;

public class ConfigurationUpdateOperationTest {

    private static final String OPERATION_ID = "id";
    private static final OperationStatus OPERATION_STATUS = OperationStatus.EXECUTING;
    private static final String DEVICE_ID = "deviceId";
    private static final String DELIVERY_TYPE = "deliveryType";
    private static final String CONFIGURATION = "test configuration";

    private ConfigurationUpdateOperation operation;

    @Before
    public void setUp() {
        operation = new ConfigurationUpdateOperation();
        operation.setId(OPERATION_ID);
        operation.setStatus(OPERATION_STATUS);
        operation.setDeviceId(DEVICE_ID);
        operation.setDeliveryType(DELIVERY_TYPE);
        operation.setConfiguration(CONFIGURATION);
    }
    
    @Test
    public void testGetConfigurationNoFragment() {
        ConfigurationUpdateOperation operation = new ConfigurationUpdateOperation();
        operation.removeProperty(operation.getOperationName());
        
        assertEquals("", operation.getConfiguration());
    }

    @Test
    public void testGetConfigurationNullFragment() {
        ConfigurationUpdateOperation operation = new ConfigurationUpdateOperation();
        operation.setProperty(operation.getOperationName(), null);
        
        assertEquals("", operation.getConfiguration());
    }

    @Test
    public void testGetConfigurationFragmentWrongType() {
        ConfigurationUpdateOperation operation = new ConfigurationUpdateOperation();
        operation.setProperty(operation.getOperationName(), new Integer(5));
        
        assertEquals("", operation.getConfiguration());
    }

    @Test
    public void testGetConfigurationNullConfiguration() {
        @SuppressWarnings("unchecked")
        Map<String, Object> fragment = (Map<String, Object>)operation.getProperty(operation.getOperationName());
        fragment.put("config", null);
        
        assertEquals("", operation.getConfiguration());
    }

    @Test
    public void testGetConfigurationNoStringConfiguration() {
        @SuppressWarnings("unchecked")
        Map<String, Object> fragment = (Map<String, Object>)operation.getProperty(operation.getOperationName());
        fragment.put("config", new Integer(5));
        
        assertEquals("", operation.getConfiguration());
    }

    @Test
    public void testGetConfiguration() {
        assertEquals(CONFIGURATION, operation.getConfiguration());
    }

    @Test
    public void testSetConfigurationNull() {
        operation.setConfiguration(null);
        
        assertEquals("", operation.getConfiguration());
    }
}
