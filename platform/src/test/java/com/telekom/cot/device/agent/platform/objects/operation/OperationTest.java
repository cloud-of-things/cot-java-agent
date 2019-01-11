package com.telekom.cot.device.agent.platform.objects.operation;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.telekom.cot.device.agent.platform.objects.operation.Operation;
import com.telekom.cot.device.agent.platform.objects.operation.OperationAttributes;
import com.telekom.cot.device.agent.platform.objects.operation.Operation.OperationStatus;


public class OperationTest {

    private final static String OPERATION_NAME = "test_operation";
    
    @Test
    public void testConstructor() {
        Operation operation = new Operation() {};
        assertEquals(0, operation.getProperties().entrySet().size());
    }

    @Test
    public void testConstructorWithId() {
        Operation operationIdNull = new Operation((String)null) {};
        Operation operationIdEmpty = new Operation("") {};
        Operation operationId = new Operation("12345") {};
        
        assertEquals(0, operationIdNull.getProperties().entrySet().size());
        assertEquals(0, operationIdEmpty.getProperties().entrySet().size());
        assertEquals("12345", operationId.getId());
    }
    
    @Test
    public void testGetOperationNameNoAnnotation() {
        Operation operation = new Operation() {};
        
        assertEquals("", operation.getOperationName());
    }
    
    @Test
    public void testGetOperationName() {
        Operation operation = new AnnotatedOperation();
        
        assertEquals(OPERATION_NAME, operation.getOperationName());
    }
    
    @Test
    public void testSetAndGetPropertiesMap() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("property1", "value1");
        properties.put("property2", "value2");
        properties.put("property3", "value3");
        
        Operation operation = new Operation() {};
        operation.setProperties(properties);
        Map<String, Object> resultProperties = operation.getProperties();
        
        assertEquals(properties, resultProperties);
        assertTrue(resultProperties.containsKey("property1"));
        assertEquals("value1", resultProperties.get("property1"));
        assertTrue(resultProperties.containsKey("property2"));
        assertEquals("value2", resultProperties.get("property2"));
        assertTrue(resultProperties.containsKey("property3"));
        assertEquals("value3", resultProperties.get("property3"));
    }

    @Test
    public void testSetPropertiesMapNull() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("property1", "value1");
        
        Operation operation = new Operation() {};
        operation.setProperties(properties);
        operation.setProperties(null);
        Map<String, Object> resultProperties = operation.getProperties();

        assertNotNull(resultProperties);
        assertTrue(resultProperties.containsKey("property1"));
        assertEquals("value1", resultProperties.get("property1"));
    }

    @Test
    public void testSetAndGetPropertyNullOrEmptyParameters() {
        Operation operation = new Operation() {};
        operation.setProperty("property1", "value1");
        
        assertNull(operation.getProperty(null));
        assertNull(operation.getProperty(null, null));
        assertNull(operation.getProperty(null, String.class));
        assertNull(operation.getProperty("property1", null));
        assertNull(operation.getProperty(""));
        assertNull(operation.getProperty("", null));
        assertNull(operation.getProperty("", String.class));
    }

    @Test
    public void testSetAndGetProperty() {
        Operation operation = new Operation() {};
        operation.setProperty("property1", "value1");
        operation.setProperty("property2", "value2");
        
        assertEquals("value1", operation.getProperty("property1"));
        assertEquals("value1", operation.getProperty("property1", String.class));
        assertEquals("value2", operation.getProperty("property2"));
        assertEquals("value2", operation.getProperty("property2", String.class));
        assertNull(operation.getProperty("property3"));
        assertNull(operation.getProperty("property3", String.class));
    }

    @Test
    public void testGetPropertyWrongType() {
        Operation operation = new Operation() {};
        operation.setProperty("property1", "value1");
        
        assertNull(operation.getProperty("property1", Integer.class));
    }

    @Test
    public void testRemoveProperty() {
        Operation operation = new Operation() {};
        operation.setProperty("property1", "value1");
        operation.setProperty("property2", "value2");
        operation.removeProperty("property1");

        assertNull(operation.getProperty("property1"));
        assertNull(operation.getProperty("property1", String.class));
        assertEquals("value2", operation.getProperty("property2"));
        assertEquals("value2", operation.getProperty("property2", String.class));
    }

    @Test
    public void testCommonProperties() {
        Operation operation = new Operation() {};
        operation.setId("id");
        operation.setStatus(OperationStatus.EXECUTING);
        operation.setDeviceId("deviceId");
        operation.setDeliveryType("deliveryType");

        assertEquals("id", operation.getId());
        assertEquals(OperationStatus.EXECUTING, operation.getStatus());
        assertEquals("deviceId", operation.getDeviceId());
        assertEquals("deliveryType", operation.getDeliveryType());
    }
    
    @Test
    public void testGetFragmentByOperationNameNoName() {
        Operation operation = new Operation() {};

        assertNull(operation.getFragmentByOperationName());
    }
    
    @Test
    public void testGetFragmentByOperationNameNoFragment() {
        AnnotatedOperation operation = new AnnotatedOperation();

        assertNull(operation.getFragmentByOperationName());
    }
    
    @Test
    public void testGetFragmentByOperationNameFragmentNull() {
        AnnotatedOperation operation = new AnnotatedOperation();
        operation.setProperty(OPERATION_NAME, null);
        
        assertNull(operation.getFragmentByOperationName());
    }
    
    @Test
    public void testGetFragmentByOperationNameFragmentWrongType() {
        AnnotatedOperation operation = new AnnotatedOperation();
        operation.setProperty(OPERATION_NAME, "no map");
        
        assertNull(operation.getFragmentByOperationName());
    }
    
    @Test
    public void testGetFragmentByOperationName() {
        HashMap<String, Object> fragmentMap = new HashMap<>();
        AnnotatedOperation operation = new AnnotatedOperation();
        operation.setProperty(OPERATION_NAME, fragmentMap);
        Map<String, Object> resultFragmentMap = operation.getFragmentByOperationName();
        
        assertNotNull(resultFragmentMap);
        assertSame(fragmentMap, resultFragmentMap);
    }
    
    @Test
    public void testGetPropertyValueFromFragmentNoPropertyName() {
        HashMap<String, Object> fragmentMap = new HashMap<>();
        fragmentMap.put("testProperty", "value");
        
        AnnotatedOperation operation = new AnnotatedOperation();
        operation.setProperty(OPERATION_NAME, fragmentMap);
        
        assertEquals("", operation.getPropertyValueFromFragment(null, "", String.class));
    }

    @Test
    public void testGetPropertyValueFromFragmentNoType() {
        HashMap<String, Object> fragmentMap = new HashMap<>();
        fragmentMap.put("testProperty", "value");
        
        AnnotatedOperation operation = new AnnotatedOperation();
        operation.setProperty(OPERATION_NAME, fragmentMap);
        
        assertEquals("", operation.getPropertyValueFromFragment("testProperty", "", null));
    }

    @Test
    public void testGetPropertyValueFromFragmentWrongType() {
        HashMap<String, Object> fragmentMap = new HashMap<>();
        fragmentMap.put("testProperty", "value");
        
        AnnotatedOperation operation = new AnnotatedOperation();
        operation.setProperty(OPERATION_NAME, fragmentMap);
        
        assertEquals(new Integer(-1), operation.getPropertyValueFromFragment("testProperty", -1, Integer.class));
    }

    @Test
    public void testGetPropertyValueFromFragment() {
        HashMap<String, Object> fragmentMap = new HashMap<>();
        fragmentMap.put("testProperty", "value");
        
        AnnotatedOperation operation = new AnnotatedOperation();
        operation.setProperty(OPERATION_NAME, fragmentMap);
        String value = operation.getPropertyValueFromFragment("testProperty", null, String.class);
        
        assertNotNull(value);
        assertEquals("value", value);
    }
    
    @OperationAttributes(name = OPERATION_NAME)
    private class AnnotatedOperation extends Operation {
    }
}
