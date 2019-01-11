package com.telekom.cot.device.agent.operation.operations;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.telekom.cot.device.agent.operation.operations.TestOperation.GivenStatus;

public class TestOperationTest {

    private TestOperation operation;
    
    @Before
    public void setUp() {
        operation = new TestOperation();
    }
    
    @Test
    public void testGetGivenStatusNoFragment() {
        operation.removeProperty(operation.getOperationName());
        assertNull(operation.getGivenStatus());
    }
    
    @Test
    public void testGetGivenStatusNoFragmentMap() {
        operation.setProperty(operation.getOperationName(), "fragment");
        assertNull(operation.getGivenStatus());
    }
    
    @Test
    public void testGetGivenStatusEmptyFragment() {
        assertNull(operation.getGivenStatus());
    }
    
    @Test
    public void testGetGivenStatusNull() {
        HashMap<String, Object> fragment = new HashMap<>();
        fragment.put("givenStatus", null);
        operation.setProperty(operation.getOperationName(), fragment);
        assertNull(operation.getGivenStatus());
    }

    @Test
    public void testGetGivenStatusWrongType() {
        HashMap<String, Object> fragment = new HashMap<>();
        fragment.put("givenStatus", new Integer(1));
        operation.setProperty(operation.getOperationName(), fragment);
        assertNull(operation.getGivenStatus());
    }

    @Test
    public void testGetGivenStatusUnknownStatus() {
        HashMap<String, Object> fragment = new HashMap<>();
        fragment.put("givenStatus", "unknown");
        operation.setProperty(operation.getOperationName(), fragment);
        assertNull(operation.getGivenStatus());
    }

    @Test
    public void testGetGivenStatusEnumStatus() {
        HashMap<String, Object> fragment = new HashMap<>();
        fragment.put("givenStatus", GivenStatus.GIVEN_SUCCESSFUL);
        operation.setProperty(operation.getOperationName(), fragment);
        assertEquals(GivenStatus.GIVEN_SUCCESSFUL, operation.getGivenStatus());
    }

    @Test
    public void testGetGivenStatusStringStatus() {
        HashMap<String, Object> fragment = new HashMap<>();
        fragment.put("givenStatus", "GIVEN_SUCCESSFUL");
        operation.setProperty(operation.getOperationName(), fragment);
        assertEquals(GivenStatus.GIVEN_SUCCESSFUL, operation.getGivenStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetGivenStatusEnumNull() {
        operation.setGivenStatus((GivenStatus)null);
        assertNull(((Map<String, Object>)operation.getProperty(operation.getOperationName())).get("givenStatus"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetGivenStatusEnum() {
        operation.setGivenStatus(GivenStatus.GIVEN_SUCCESSFUL);
        assertEquals(GivenStatus.GIVEN_SUCCESSFUL, ((Map<String, Object>)operation.getProperty(operation.getOperationName())).get("givenStatus"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetGivenStatusStringNull() {
        operation.setGivenStatus((String)null);
        assertNull(((Map<String, Object>)operation.getProperty(operation.getOperationName())).get("givenStatus"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetGivenStatusUnknown() {
        operation.setGivenStatus("unknown");
        assertNull(((Map<String, Object>)operation.getProperty(operation.getOperationName())).get("givenStatus"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetGivenStatusString() {
        operation.setGivenStatus("GIVEN_SUCCESSFUL");
        assertEquals(GivenStatus.GIVEN_SUCCESSFUL, ((Map<String, Object>)operation.getProperty(operation.getOperationName())).get("givenStatus"));
    }
}
