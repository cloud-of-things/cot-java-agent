package com.telekom.cot.device.agent.platform.objects;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.telekom.cot.device.agent.platform.objects.AgentMobile;


public class AgentMobileTest {
    //test AgentMobile

    private static final String imsi = "testImsi";
    private static final String imei = "testImei";
    private static final String currentOperator = "testOperator";
    private static final String currentBand = "testBand";
    private static final String connType = "testType";
    private static final String rssi = "testRssi";
    private static final String ecn0 = "testEcn0";
    private static final String rcsp = "testRcsp";
    private static final String mnc = "testMnc";
    private static final String lac = "testLac";
    private static final String cellId = "testCellId";
    private static final String msisdn = "testMsisdn";
    private static final String iccid = "testIccid";
    
    private boolean isComplete = true;

    
    AgentMobile testMobileFull = new AgentMobile(imsi, imei, currentOperator, currentBand, connType, rssi, ecn0,
                                 rcsp, mnc, lac, cellId, msisdn, iccid);
    
    private JsonElement getTestJson() {
        JsonObject testObject = new JsonObject();
        testObject.addProperty("imei", imei);
        testObject.addProperty("cellId", cellId);
        testObject.addProperty("iccid", iccid);

        if (isComplete) {
            testObject.addProperty("imsi", imsi);
            testObject.addProperty("currentOperator", currentOperator);
            testObject.addProperty("currentBand", currentBand);
            testObject.addProperty("connType", connType);
            testObject.addProperty("rssi", rssi);
            testObject.addProperty("ecn0", ecn0);
            testObject.addProperty("rcsp", rcsp);
            testObject.addProperty("mnc", mnc);
            testObject.addProperty("lac", lac);
            testObject.addProperty("msisdn", msisdn);
        }
        return testObject;
    }

    
    @Test
    public void testGetterAndSetter() {
        
        assertEquals(imei, testMobileFull.getImei() );
        assertEquals(cellId, testMobileFull.getCellId() );
        assertEquals(iccid, testMobileFull.getIccid() );
        assertEquals("c8y_Mobile", testMobileFull.getId() );
        assertEquals(getTestJson(), testMobileFull.getJson() );
        
    }
}
