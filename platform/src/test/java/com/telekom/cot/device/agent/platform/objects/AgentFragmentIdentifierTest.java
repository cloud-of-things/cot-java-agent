package com.telekom.cot.device.agent.platform.objects;

import static org.junit.Assert.*;

import org.junit.Test;

import com.telekom.cot.device.agent.platform.objects.AgentFragmentIdentifier;


public class AgentFragmentIdentifierTest {
    
    @Test
    public void testEnum() {
        //test enum
        assertEquals("c8y_Hardware", AgentFragmentIdentifier.HARDWARE.getId());
        assertEquals("c8y_Configuration", AgentFragmentIdentifier.CONFIGURATION.getId());
        assertEquals("c8y_Firmware", AgentFragmentIdentifier.FIRMWARE.getId());
        assertEquals("c8y_Mobile", AgentFragmentIdentifier.MOBILE.getId());
        assertEquals("c8y_SoftwareList", AgentFragmentIdentifier.SOFTWARE_LIST.getId());
    }
}
