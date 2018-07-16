package com.telekom.cot.device.agent.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class ChecksumAlgorithmTest {
    @Test
    public void testEnum() {
        assertEquals(ChecksumAlgorithm.MD5, ChecksumAlgorithm.valueOf("MD5"));
        assertEquals(ChecksumAlgorithm.SHA1, ChecksumAlgorithm.valueOf("SHA1"));
        assertEquals(ChecksumAlgorithm.SHA256, ChecksumAlgorithm.valueOf("SHA256"));

        try {
            ChecksumAlgorithm.valueOf("SHA-256");
            fail();
        } catch (IllegalArgumentException exc) {
        }
    }
}
