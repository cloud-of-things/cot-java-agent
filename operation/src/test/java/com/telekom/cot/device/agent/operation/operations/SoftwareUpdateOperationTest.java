package com.telekom.cot.device.agent.operation.operations;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.telekom.cot.device.agent.system.properties.Software;

public class SoftwareUpdateOperationTest {

    private static final String SOFTWARE_NAME = "testSoftware";
    private static final String SOFTWARE_VERSION = "0815";
    private static final String SOFTWARE_URL = "https://1.2.3.4";
    
    private SoftwareUpdateOperation operation;
    private JsonArray softwareJsonArray;
    
    @Before
    public void setUp() {
        operation = new SoftwareUpdateOperation();

        softwareJsonArray = new JsonArray();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", SOFTWARE_NAME);
        jsonObject.addProperty("version", SOFTWARE_VERSION);
        jsonObject.addProperty("url", SOFTWARE_URL);
        softwareJsonArray.add(jsonObject);

        operation.setProperty(operation.getOperationName(), softwareJsonArray);
    }
    
    @Test
    public void testGetSoftwareNoFragment() {
        operation.removeProperty(operation.getOperationName());
        
        assertNull(operation.getSoftware());
    }

    @Test
    public void testGetSoftwareNullFragment() {
        operation.setProperty(operation.getOperationName(), null);
        
        assertNull(operation.getSoftware());
    }

    @Test
    public void testGetSoftwareFragmentWrongType() {
        operation.setProperty(operation.getOperationName(), new Integer(1));
        
        assertNull(operation.getSoftware());
    }

    @Test
    public void testGetSoftwareEmptyArray() {
        operation.setProperty(operation.getOperationName(), new JsonArray());
        
        assertNull(operation.getSoftware());
    }

    @Test
    public void testGetSoftwareNoSoftwareInfo() {
        softwareJsonArray.remove(0);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("test", "test");
        softwareJsonArray.add(jsonObject);

        assertNull(operation.getSoftware());
    }

    @Test
    public void testGetSoftwareTwoSoftwareInfos() {
        softwareJsonArray.add(softwareJsonArray.get(0));

        assertNull(operation.getSoftware());
    }

    @Test
    public void testGetSoftware() {
        Software software = operation.getSoftware();
        
        assertNotNull(software);
        assertEquals(SOFTWARE_NAME, software.getName());
        assertEquals(SOFTWARE_VERSION, software.getVersion());
        assertEquals(SOFTWARE_URL, software.getUrl());
    }
}
