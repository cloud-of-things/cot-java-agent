package com.telekom.cot.device.agent.system.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;


public class SoftwarePropertiesTest {

    private static final String NAME = "testName";
    private static final String VERSION = "testVersion";
    private static final String URL = "testUrl";
    private static final Software SOFTWARE = new Software(NAME, VERSION, URL);
    private static final String EXPECTED_STRING_SOFTWARE_PROPERTIES = "SoftwareProperties [softwareList=[" + SOFTWARE.toString() + "]]";

    private SoftwareProperties softwareProperties;

    @Before
    public void setUp() {
        softwareProperties = new SoftwareProperties();
        softwareProperties.addSoftware(NAME, VERSION, URL);
    }

    @Test
    public void testAddSoftwareNoName() {
        softwareProperties = new SoftwareProperties();
        softwareProperties.addSoftware(null, VERSION, URL);
        assertEquals(0, softwareProperties.getSoftwareList().size());

        softwareProperties = new SoftwareProperties();
        softwareProperties.addSoftware("", VERSION, URL);
        assertEquals(0, softwareProperties.getSoftwareList().size());
    }    
    
    @Test
    public void testAddSoftwareNoVersion() {
        softwareProperties = new SoftwareProperties();
        softwareProperties.addSoftware(NAME, null, URL);
        assertEquals(0, softwareProperties.getSoftwareList().size());

        softwareProperties = new SoftwareProperties();
        softwareProperties.addSoftware(NAME, "", URL);
        assertEquals(0, softwareProperties.getSoftwareList().size());
    }    
    
    @Test
    public void testAddSoftwareNoUrl() {
        softwareProperties = new SoftwareProperties();
        softwareProperties.addSoftware(NAME, VERSION, null);
        assertEquals(1, softwareProperties.getSoftwareList().size());

        softwareProperties = new SoftwareProperties();
        softwareProperties.addSoftware(NAME, VERSION, "");
        assertEquals(1, softwareProperties.getSoftwareList().size());
    }    
    
    @Test
    public void testAddSoftwareDuplicateName() {
        softwareProperties = new SoftwareProperties();
        softwareProperties.addSoftware(NAME, VERSION, null);
        softwareProperties.addSoftware(NAME, VERSION + "-234", "");
        List<Software> softwareList = softwareProperties.getSoftwareList();

        assertEquals(1, softwareList.size());
        assertEquals(NAME, softwareList.get(0).getName());
        assertEquals(VERSION, softwareList.get(0).getVersion());
        assertNull(softwareList.get(0).getUrl());
    }    
    
    @Test
    public void testGetters() {
        //test getters
        assertEquals(NAME, softwareProperties.getSoftwareList().get(0).getName());
        assertEquals(VERSION, softwareProperties.getSoftwareList().get(0).getVersion());
        assertEquals(URL, softwareProperties.getSoftwareList().get(0).getUrl());
    }

    @Test
    public void testToString() {
        assertEquals(EXPECTED_STRING_SOFTWARE_PROPERTIES, softwareProperties.toString());
    }
}