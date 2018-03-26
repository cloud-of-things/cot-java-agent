package com.telekom.cot.device.agent.common.exc;

import org.junit.Assert;
import org.junit.Test;

public class AgentShutdownExceptionTest {

	@Test
	public void test() {
		AgentShutdownException agentShutdownException = new AgentShutdownException("testMessage");
		Assert.assertEquals("testMessage", agentShutdownException.getMessage());

		agentShutdownException = new AgentShutdownException("testMessage", new Throwable());
		Assert.assertEquals("testMessage", agentShutdownException.getMessage());

		agentShutdownException = new AgentShutdownException("testMessage", new Throwable(), true, true);
		Assert.assertEquals("testMessage", agentShutdownException.getMessage());
	}
}
