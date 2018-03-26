package com.telekom.cot.device.agent.app;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.telekom.cot.device.agent.service.configuration.Configuration;
import com.telekom.cot.device.agent.service.configuration.ConfigurationPath;

@ConfigurationPath("agent.common")
public class CommonConfiguration implements Configuration {

	@NotNull @Positive
	private Integer shutdownTimeout;

	public Integer getShutdownTimeout() {
		return shutdownTimeout;
	}

	public void setShutdownTimeout(Integer shutdownTimeout) {
		this.shutdownTimeout = shutdownTimeout;
	}
}
