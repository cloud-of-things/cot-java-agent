package com.telekom.cot.device.agent.platform;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.telekom.cot.device.agent.platform.objects.AgentHardware;


public class AgentHardwareTest {
    //test AgentHardware
    
    private static final String model = "testModell";
    private static final String revision = "testRevision";
    private static final String serialNumber = "testSerial";
    
    private AgentHardware testHardware = new AgentHardware(model, revision, serialNumber);
    
    private JsonElement getTestJson() {
        JsonObject testObject = new JsonObject();
        testObject.addProperty("model", model);
        testObject.addProperty("revision", revision);
        testObject.addProperty("serialNumber", serialNumber);

        return testObject;
    }

    @Test
    public void testGetterAndSetter() {
        assertEquals(model, testHardware.getModel());
        assertEquals(revision, testHardware.getRevision());
        assertEquals(serialNumber, testHardware.getSerialNumber());
        assertEquals("c8y_Hardware", testHardware.getId());
        assertEquals(getTestJson(), testHardware.getJson());
    }
}
