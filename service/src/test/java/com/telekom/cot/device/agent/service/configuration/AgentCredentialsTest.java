package com.telekom.cot.device.agent.service.configuration;

import static org.junit.Assert.*;

import org.junit.Test;

public class AgentCredentialsTest {

	private static final String PASSWORD = "testPassword";
	private static final String USER = "testUser";
	private static final String TENANT = "testTenant";

	@Test
	public void testDefaultConstructor() {
		AgentCredentials credentials = new AgentCredentials();

		assertEquals(null, credentials.getTenant());
		assertEquals(null, credentials.getUsername());
		assertEquals(null, credentials.getPassword());
	}

	@Test
	public void testValueConstructor() {
		AgentCredentials credentials = new AgentCredentials(TENANT, USER, PASSWORD);

		assertEquals(TENANT, credentials.getTenant());
		assertEquals(USER, credentials.getUsername());
		assertEquals(PASSWORD, credentials.getPassword());
	}

	@Test
	public void testSettersAndGetters() {
		AgentCredentials credentials = new AgentCredentials();
		credentials.setTenant(TENANT);
		credentials.setUsername(USER);
		credentials.setPassword(PASSWORD);

		assertEquals(TENANT, credentials.getTenant());
		assertEquals(USER, credentials.getUsername());
		assertEquals(PASSWORD, credentials.getPassword());
	}
	
	@Test
	public void testToString() {
		AgentCredentials credentials = new AgentCredentials(TENANT, USER, PASSWORD);
		String expected = AgentCredentials.class.getSimpleName() + " [tenant=" + TENANT + ", username=" + USER + ", password=" + PASSWORD + "]";

		assertEquals(expected, credentials.toString());
	}
}
