package com.telekom.cot.device.agent.common.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.ConfigurationNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class AssertionUtilTest {

	@Mock Logger mockLogger;

	@Test
	public void testAssertIsTrue() throws AbstractAgentException {
		AssertionUtil.assertIsTrue(true, ConfigurationNotFoundException.class, mockLogger, "Test");
	}

	@Test(expected = ConfigurationNotFoundException.class)
	public void testAssertIsTrueException() throws AbstractAgentException {
		AssertionUtil.assertIsTrue(false, ConfigurationNotFoundException.class, mockLogger, "Test");
	}

	@Test
	public void testAssertNotNull() throws AbstractAgentException {
		AssertionUtil.assertNotNull("not null", ConfigurationNotFoundException.class, mockLogger, "Test");
	}

	@Test(expected = ConfigurationNotFoundException.class)
	public void testAssertNotNullException() throws AbstractAgentException {
		AssertionUtil.assertNotNull(null, ConfigurationNotFoundException.class, mockLogger, "Test");
	}

	@Test
	public void testAssertNotEmpty() throws AbstractAgentException {
		AssertionUtil.assertNotEmpty("not empty", ConfigurationNotFoundException.class, mockLogger, "Test");

		Map<String, String> stringMap = new HashMap<>();
		stringMap.put("test", "value");
		AssertionUtil.assertNotEmpty(stringMap, ConfigurationNotFoundException.class, mockLogger, "Test");
	}

	@Test(expected = ConfigurationNotFoundException.class)
	public void testAssertNotEmptyException() throws AbstractAgentException {
		AssertionUtil.assertNotEmpty("", ConfigurationNotFoundException.class, mockLogger, "Test");
	}

	@Test(expected = ConfigurationNotFoundException.class)
	public void testAssertNotEmptyMapNullException() throws AbstractAgentException {
		Map<String, String> stringMap = null;
		AssertionUtil.assertNotEmpty(stringMap, ConfigurationNotFoundException.class, mockLogger, "Test");
	}

	@Test(expected = ConfigurationNotFoundException.class)
	public void testAssertNotEmptyMapEmptyException() throws AbstractAgentException {
		Map<String, String> stringMap = new HashMap<>();
		AssertionUtil.assertNotEmpty(stringMap, ConfigurationNotFoundException.class, mockLogger, "Test");
	}

	@Test(expected = ConfigurationNotFoundException.class)
	public void testCreateExceptionAndLog() throws AbstractAgentException {
		AbstractAgentException testException = AssertionUtil.createExceptionAndLog(ConfigurationNotFoundException.class, mockLogger, "Test", new Throwable());
		throw testException;
	}

	@Test(expected = AbstractAgentException.class)
	public void testCreateExceptionConstructorNotFound() throws AbstractAgentException {
		AbstractAgentException testException = AssertionUtil.createExceptionAndLog(TestException.class, mockLogger, "Test", new Throwable());
		throw testException;
	}


	public class TestException extends AbstractAgentException {
        private static final long serialVersionUID = -668908809018749094L;

        public TestException(String message) {
			super(message);
		}
	}

}
