package com.telekom.cot.device.agent.system.properties;

import static org.junit.Assert.*;

import org.junit.Test;


public class SoftwareTest {

    private static final String NAME = "testName";
    private static final String VERSION = "testVersion";
    private static final String URL = "testUrl";
    private static final String EXPECTED_STRING = "Software [name=" + NAME + ", version=" + VERSION + ", url=" + URL + "]";

    @Test
    public void testDefaultConstructor() {
        Software software = new Software();
        
        assertNull(software.getName());
        assertNull(software.getVersion());
        assertNull(software.getUrl());
    }
    
    @Test
    public void testConstructor() {
        Software software = new Software(NAME, VERSION, URL);
        
        assertEquals(NAME, software.getName());
        assertEquals(VERSION, software.getVersion());
        assertEquals(URL, software.getUrl());
    }
    
    @Test
    public void testSettersAndGetters() {
        Software software = new Software();
        software.setName(NAME);
        software.setVersion(VERSION);
        software.setUrl(URL);
        
        assertEquals(NAME, software.getName());
        assertEquals(VERSION, software.getVersion());
        assertEquals(URL, software.getUrl());
    }

    @Test
    public void testToString() {
        Software software = new Software(NAME, VERSION, URL);
        
        assertEquals(EXPECTED_STRING, software.toString());
    }
}
