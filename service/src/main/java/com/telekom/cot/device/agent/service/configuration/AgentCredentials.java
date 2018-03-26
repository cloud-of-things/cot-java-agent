package com.telekom.cot.device.agent.service.configuration;

import javax.validation.constraints.*;

@ConfigurationPath(value="agent.credentials")
public class AgentCredentials implements Configuration {

	@NotNull @NotEmpty private String tenant;
	@NotNull @NotEmpty private String username;
	@NotNull @NotEmpty private String password;

	public AgentCredentials() {
	}

	public AgentCredentials(String tenant, String username, String password) {
		this.tenant = tenant;
		this.username = username;
		this.password = password;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return AgentCredentials.class.getSimpleName() + " [tenant=" + tenant + ", username=" + username + ", password=" + password + "]";
	}
}
