package com.telekom.cot.device.agent.platform.objects;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.telekom.cot.device.agent.platform.objects.AgentSoftwareList;
import com.telekom.cot.device.agent.platform.objects.AgentSoftwareList.Software;


public class AgentSoftwareListTest {
    
    private static final String name = "testName";
    private static final String url = "testUrl";
    private static final String version = "testVersion";
    private static final String id = "c8y_SoftwareList";
    
    AgentSoftwareList testAgentSoftware = new AgentSoftwareList();
    Software testSoftware;
    
    @Before
    public void setUp() {
      
    }

    @Test
    public void testGetterAndSetter() {
        testSoftware = new Software(name, version, url);
        testAgentSoftware.addSoftware(testSoftware);
        
        //assertEquals(, testAgentSoftware.getSoftwareList() );
        assertEquals(id, testAgentSoftware.getId() );
    }
}
