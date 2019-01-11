package com.telekom.cot.device.agent.platform.objects;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.telekom.cot.device.agent.platform.objects.AgentManagedObject;


public class AgentManagedObjectTest {
    
    private static final String name = "testName";
    private static final String id = "testId";
    private static final String type = "testType";
    private static final String attributeId = "testAttribute";
    private static final String value = "testValue";
    
    private static final HashMap<String, Object> testMap = new HashMap<>();
    
    AgentManagedObject testObject = new AgentManagedObject();   
    
    @Before
    public void setUp() {
        testMap.put("name", name);
        testMap.put("id", id);
        testMap.put("type", type);
        testMap.put(attributeId, value);
    }
    @Test
    public void testGetterAndSetter() {
        testObject.setId(id);
        testObject.setName(name);
        testObject.setType(type);
        
        assertEquals(testMap.get("name"), testObject.getName() );
        assertEquals(testMap.get("id"), testObject.getId() );
        assertEquals(testMap.get("type"), testObject.getType() );
    }
    
    @Test
    public void testAttribute() {
        testObject.set(attributeId, value);
        testObject.setAttributes(testMap);
        assertEquals(testMap, testObject.getAttributes() );
    }
}