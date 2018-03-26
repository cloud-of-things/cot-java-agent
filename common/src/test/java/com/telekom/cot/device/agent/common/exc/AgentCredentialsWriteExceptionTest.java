package com.telekom.cot.device.agent.common.exc;

import org.junit.Assert;
import org.junit.Test;

public class AgentCredentialsWriteExceptionTest {

	@Test
	public void test() {
		AgentCredentialsWriteException agentCredentialsWriteException = new AgentCredentialsWriteException("testMessage");
		Assert.assertEquals("testMessage", agentCredentialsWriteException.getMessage());

		agentCredentialsWriteException = new AgentCredentialsWriteException("testMessage", new Throwable());
		Assert.assertEquals("testMessage", agentCredentialsWriteException.getMessage());

		agentCredentialsWriteException = new AgentCredentialsWriteException("testMessage", new Throwable(), true, true);
		Assert.assertEquals("testMessage", agentCredentialsWriteException.getMessage());
	}
}
