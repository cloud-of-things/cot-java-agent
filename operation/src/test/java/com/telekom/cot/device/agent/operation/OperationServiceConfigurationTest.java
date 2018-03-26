package com.telekom.cot.device.agent.operation;

import static org.junit.Assert.*;

import com.telekom.cot.device.agent.common.util.ValidationUtil;
import org.junit.Before;
import org.junit.Test;

public class OperationServiceConfigurationTest {

	OperationServiceConfiguration operationServiceConfiguration;

	private final Integer interval = 1;
	private final Integer resultSize = 2;
	private final Integer shutdownTimeout = 3;
	private final Integer handlersShutdownTimeout = 4;
	
	private final String expectedToString = "OperationServiceConfiguration [interval=1, resultSize=2, shutdownTimeout=3, handlersShutdownTimeout=4]";


	@Before
	public void setup() {
		operationServiceConfiguration = new OperationServiceConfiguration();
		operationServiceConfiguration.setInterval(interval);
		operationServiceConfiguration.setResultSize(resultSize);
		operationServiceConfiguration.setShutdownTimeout(shutdownTimeout);
		operationServiceConfiguration.setHandlersShutdownTimeout(handlersShutdownTimeout);
	}

	@Test
	public void nullTest() { // all integers unset
		OperationServiceConfiguration nullConfiguration = new OperationServiceConfiguration();
		assertFalse(ValidationUtil.isValid(nullConfiguration));
	}

	@Test
	public void nullIntervalTest() {
		operationServiceConfiguration.setInterval(null);
		assertFalse(ValidationUtil.isValid(operationServiceConfiguration));
	}

	@Test
	public void nullResultSizeTest() {
		operationServiceConfiguration.setResultSize(null);
		assertFalse(ValidationUtil.isValid(operationServiceConfiguration));
	}

	@Test
	public void nullShutdownTimeoutTest() {
		operationServiceConfiguration.setShutdownTimeout(null);
		assertFalse(ValidationUtil.isValid(operationServiceConfiguration));
	}

	@Test
	public void nullHandlersShutdownTimeoutTest() {
		operationServiceConfiguration.setHandlersShutdownTimeout(null);
		assertFalse(ValidationUtil.isValid(operationServiceConfiguration));
	}

	@Test
	public void negativeTest() {
		// interval negative
		operationServiceConfiguration.setInterval(-1);
		assertFalse(ValidationUtil.isValid(operationServiceConfiguration));

		// result size negative
		operationServiceConfiguration.setResultSize(-1);
		assertFalse(ValidationUtil.isValid(operationServiceConfiguration));

		// shutdownTimeout negative
		operationServiceConfiguration.setShutdownTimeout(-1);
		assertFalse(ValidationUtil.isValid(operationServiceConfiguration));

		// handlersShutdownTimeout negative
		operationServiceConfiguration.setHandlersShutdownTimeout(-1);
		assertFalse(ValidationUtil.isValid(operationServiceConfiguration));
	}

	@Test
	public void testGettersAndSetters() {
		assertEquals(interval, operationServiceConfiguration.getInterval());
		assertEquals(resultSize, operationServiceConfiguration.getResultSize());
		assertEquals(shutdownTimeout, operationServiceConfiguration.getShutdownTimeout());
		assertEquals(handlersShutdownTimeout, operationServiceConfiguration.getHandlersShutdownTimeout());
	}

	@Test
	public void testToString() {
		assertEquals(expectedToString, operationServiceConfiguration.toString());
	}

	@Test
	public void validate() {
		assertTrue(ValidationUtil.isValid(operationServiceConfiguration));
	}

}
