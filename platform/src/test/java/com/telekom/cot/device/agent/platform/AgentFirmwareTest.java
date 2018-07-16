package com.telekom.cot.device.agent.platform;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.telekom.cot.device.agent.platform.objects.AgentFirmware;


public class AgentFirmwareTest {
    //test AgentFirmware
    
    //used test variables
    private static final String name = "testFirmware";
    private static final String version = "testVersion";
    private static final String url = "testUrl";
    //new AgentFirmware object with test varibales
    private AgentFirmware testFirmware = new AgentFirmware(name, version, url);
   
    //create new json object with test variables
    private JsonElement getTestJson() {
        JsonObject testObject = new JsonObject();
        testObject.addProperty("name", name);
        testObject.addProperty("version", version);
        testObject.addProperty("url", url);

        return testObject;
    }


    @Test
    public void testSettersAndGetters() {
        assertEquals(name, testFirmware.getName());
        assertEquals(version, testFirmware.getVersion());
        assertEquals(url, testFirmware.getUrl());
        assertEquals("c8y_Firmware", testFirmware.getId());
        assertEquals(getTestJson(), testFirmware.getJson());
    }
}
