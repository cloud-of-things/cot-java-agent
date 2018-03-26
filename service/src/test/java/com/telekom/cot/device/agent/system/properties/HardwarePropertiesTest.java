package com.telekom.cot.device.agent.system.properties;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class HardwarePropertiesTest {

    private static final String MODEL = "modelTest";
    private static final String REVISION = "revisionTest";
    private static final String SERIALNUMBER = "serialNumberTest";

    private HardwareProperties hardwareProperties;

    @Before
    public void setUp(){
        hardwareProperties = new HardwareProperties();
        hardwareProperties.setModel(MODEL);
        hardwareProperties.setRevision(REVISION);
        hardwareProperties.setSerialNumber(SERIALNUMBER);
    }

    @Test
    public void testDefaultConstructor(){
        hardwareProperties = new HardwareProperties();
        assertNull(hardwareProperties.getModel());
        assertNull(hardwareProperties.getRevision());
        assertNull(hardwareProperties.getSerialNumber());
    }

    @Test
    public void testConstructor(){
        hardwareProperties = new HardwareProperties(MODEL, REVISION, SERIALNUMBER);
        assertEquals(MODEL, hardwareProperties.getModel());
        assertEquals(REVISION, hardwareProperties.getRevision());
        assertEquals(SERIALNUMBER, hardwareProperties.getSerialNumber());
    }

    @Test
    public void testGetters(){
        assertEquals(MODEL, hardwareProperties.getModel());
        assertEquals(REVISION, hardwareProperties.getRevision());
        assertEquals(SERIALNUMBER, hardwareProperties.getSerialNumber());
    }

    @Test
    public void testToString(){
        String expectedToString = "HardwareProperties [model="+MODEL+", revision="+REVISION+", serialNumber="+SERIALNUMBER
                +"]";
        assertEquals(expectedToString, hardwareProperties.toString());
    }

}
