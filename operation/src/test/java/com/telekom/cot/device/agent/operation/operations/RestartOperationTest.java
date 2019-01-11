package com.telekom.cot.device.agent.operation.operations;

import static org.junit.Assert.*;

import org.junit.Test;


public class RestartOperationTest {

    @Test
    public void testRestartOperation() {
        RestartOperation operation = new RestartOperation();
        assertEquals("c8y_Restart", operation.getOperationName());
    }
}
