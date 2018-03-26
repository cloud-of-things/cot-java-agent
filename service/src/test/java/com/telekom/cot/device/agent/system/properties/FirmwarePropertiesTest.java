package com.telekom.cot.device.agent.system.properties;

import static org.junit.Assert.*;

import org.junit.Test;

public class FirmwarePropertiesTest {

    private static final String NAME = "testName";
    private static final String VERSION = "testVersion";
    private static final String TEST_URL = "testURL";
    private static final String EXPECTED_STRING = "FirmwareProperties [name="+NAME+", version="+VERSION+", url="+TEST_URL+"]";

    @Test
    public void testDefaultConstructor() {
        FirmwareProperties firmwareProperties = new FirmwareProperties();
        
        assertNull(firmwareProperties.getName());
        assertNull(firmwareProperties.getVersion());
        assertNull(firmwareProperties.getUrl());
    }

/*    @Test
    public void testConstructor() {
        firmwareProperties = new FirmwareProperties(NAME, VERSION, TEST_URL);

        assertEquals(NAME, firmwareProperties.getName());
        assertEquals(VERSION, firmwareProperties.getVersion());
        assertEquals(TEST_URL, firmwareProperties.getUrl());
    }

    @Test
    public void testSettersAndGetters() {
        firmwareProperties = new FirmwareProperties();
        firmwareProperties.setName(NAME);
        firmwareProperties.setVersion(VERSION);
        firmwareProperties.setUrl(TEST_URL);
        
        assertEquals(NAME, firmwareProperties.getName());
        assertEquals(VERSION, firmwareProperties.getVersion());
        assertEquals(TEST_URL, firmwareProperties.getUrl());
    }
*/
    @Test
    public void testToString() {
        FirmwareProperties firmwareProperties = new FirmwareProperties(NAME, VERSION, TEST_URL);
        
        assertEquals(EXPECTED_STRING, firmwareProperties.toString());
    }
}