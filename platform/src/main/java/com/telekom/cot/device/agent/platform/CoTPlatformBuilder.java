package com.telekom.cot.device.agent.platform;

import java.util.Objects;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.m2m.cot.restsdk.CloudOfThingsPlatform;
import com.telekom.m2m.cot.restsdk.devicecontrol.CotCredentials;

public class CoTPlatformBuilder {

	private String tenant;
	private String username;
	private String password;
	private String hostname;

	private CoTPlatformBuilder() {

	}

	public static CoTPlatformBuilder create() {
		return new CoTPlatformBuilder();
	}

	public CloudOfThingsPlatform build() throws AbstractAgentException {
		if (Objects.isNull(tenant) || Objects.isNull(username) || Objects.isNull(password)
				|| Objects.isNull(hostname)) {
			throw new PlatformServiceException("some attributes are missed " + toString());
		}
		
		String url = "https://" + tenant + "." + hostname;
		CotCredentials credentials = new CotCredentials(tenant, username, password);
		return new CloudOfThingsPlatform(url, credentials);
	}

	public CoTPlatformBuilder setTenant(String tenant) {
		this.tenant = tenant;
		return this;
	}

	public CoTPlatformBuilder setUsername(String username) {
		this.username = username;
		return this;
	}

	public CoTPlatformBuilder setPassword(String password) {
		this.password = password;
		return this;
	}

	public CoTPlatformBuilder setHostname(String hostname) {
		this.hostname = hostname;
		return this;
	}

	@Override
	public String toString() {
		return CoTPlatformBuilder.class.getSimpleName() + " [tenant=" + tenant + ", username=" + username + ", password="
				+ password + ", hostname=" + hostname + "]";
	}

}
