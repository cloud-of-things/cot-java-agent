package com.telekom.cot.device.agent.system.properties;

import org.junit.Assert;
import org.junit.Test;

public class ConfigurationPropertiesTest {

    private static final String CONFIG = "testConfig";
    private static final String expectedString = "ConfigurationProperties [config="+CONFIG+"]";
    ConfigurationProperties configurationProperties;


    @Test
    public void testSettersAndGetters() {
        configurationProperties = new ConfigurationProperties();
        configurationProperties.setConfig(CONFIG);
        Assert.assertEquals(CONFIG, configurationProperties.getConfig());
    }

    @Test
    public void testConstructor() {
        configurationProperties = new ConfigurationProperties(CONFIG);
        Assert.assertEquals(CONFIG, configurationProperties.getConfig());
    }

    @Test
    public void testToString() {
        configurationProperties = new ConfigurationProperties(CONFIG);
        Assert.assertEquals(expectedString, configurationProperties.toString());
    }

}