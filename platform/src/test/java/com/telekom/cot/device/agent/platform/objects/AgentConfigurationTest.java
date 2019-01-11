package com.telekom.cot.device.agent.platform.objects;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.telekom.cot.device.agent.platform.objects.AgentConfiguration;


public class AgentConfigurationTest {
    
    private static final String config = "testConfig";
    private static final String id = "c8y_Configuration";

    AgentConfiguration testConfiguration = new AgentConfiguration(config);
    
    private JsonElement getTestJson() {
        JsonObject testObject = new JsonObject();
        testObject.addProperty("config", config);
        return testObject;
    }
    
    @Test
    public void testGetterAndSetter() {
        assertEquals(config, testConfiguration.getConfig() );
        assertEquals(id, testConfiguration.getId() );
        assertEquals(getTestJson(), testConfiguration.getJson() );
    }
}
