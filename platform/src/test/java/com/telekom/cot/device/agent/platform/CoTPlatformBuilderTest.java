package com.telekom.cot.device.agent.platform;

import static org.junit.Assert.assertNotNull;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import com.telekom.cot.device.agent.common.exc.PlatformServiceException;

public class CoTPlatformBuilderTest {

	private static final String PASSWORD = "password";
	private static final String USERNAME = "username";
	private static final String TENANT = "tenant";
	private static final String HOSTNAME = "hostname";

	@Test
	public void testToString() {

		CoTPlatformBuilder builder = CoTPlatformBuilder.create().setHostname(HOSTNAME).setPassword(PASSWORD)
				.setTenant(TENANT).setUsername(USERNAME);

		Assert.assertThat(builder.toString(), Matchers.equalTo(
				"CoTPlatformBuilder [tenant=" + TENANT + ", username=" + USERNAME + ", password=" + PASSWORD + ", hostname=" + HOSTNAME + "]"));

	}

	@Test(expected=PlatformServiceException.class)
	public void testHostnameNull() throws Exception {
		CoTPlatformBuilder.create().setHostname(null).setPassword(PASSWORD).setTenant(TENANT)
			.setUsername(USERNAME).build();
	}

	@Test(expected=PlatformServiceException.class)
	public void testUsernameNull() throws Exception {
		CoTPlatformBuilder.create().setHostname(HOSTNAME).setPassword(PASSWORD).setTenant(TENANT)
			.setUsername(null).build();
	}

	@Test(expected=PlatformServiceException.class)
	public void testPasswordNull() throws Exception {
		CoTPlatformBuilder.create().setHostname(HOSTNAME).setPassword(null).setTenant(TENANT)
		.setUsername(USERNAME).build();
	}

	@Test(expected=PlatformServiceException.class)
	public void testTenantNull() throws Exception {
		CoTPlatformBuilder.create().setHostname(HOSTNAME).setPassword(PASSWORD).setTenant(null)
		.setUsername(USERNAME).build();
	}

	@Test
	public void testBuild() throws Exception {
		assertNotNull(CoTPlatformBuilder.create().setHostname(HOSTNAME).setPassword(PASSWORD).setTenant(TENANT)
							.setUsername(USERNAME).build());
	}
}
