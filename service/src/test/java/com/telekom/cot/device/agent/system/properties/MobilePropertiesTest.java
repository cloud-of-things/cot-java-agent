package com.telekom.cot.device.agent.system.properties;

import static org.junit.Assert.*;

import org.junit.Test;

public class MobilePropertiesTest {

    private static final String IMEI = "imeiTest";
    private static final String CELLID = "cellIdTest";
    private static final String ICCID = "iccidTest";

    @Test
    public void testDefaultConstructor() {
        MobileProperties mobileProperties = new MobileProperties();
        
        assertNull(mobileProperties.getImei());
        assertNull(mobileProperties.getCellId());
        assertNull(mobileProperties.getIccid());
    }
    
    @Test
    public void testConstructor() {
        MobileProperties mobileProperties = new MobileProperties(IMEI, CELLID, ICCID);
        
        assertEquals(IMEI, mobileProperties.getImei());
        assertEquals(CELLID, mobileProperties.getCellId());
        assertEquals(ICCID, mobileProperties.getIccid());
    }
    
    @Test
    public void testSettersAndGetters(){
        MobileProperties mobileProperties = new MobileProperties();
        mobileProperties.setImei(IMEI);
        mobileProperties.setCellId(CELLID);
        mobileProperties.setIccid(ICCID);

        assertEquals(IMEI, mobileProperties.getImei());
        assertEquals(CELLID, mobileProperties.getCellId());
        assertEquals(ICCID, mobileProperties.getIccid());
    }

    @Test
    public void testToString(){
        MobileProperties mobileProperties = new MobileProperties(IMEI, CELLID, ICCID);
        String expectedString = "MobileProperties [imei=" + IMEI + ", cellId=" + CELLID + ", iccid=" + ICCID + "]";

        assertEquals(expectedString, mobileProperties.toString());
    }

}
