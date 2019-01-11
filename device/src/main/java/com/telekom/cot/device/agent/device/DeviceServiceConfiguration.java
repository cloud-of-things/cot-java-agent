package com.telekom.cot.device.agent.device;

import com.telekom.cot.device.agent.common.annotations.ConfigurationPath;
import com.telekom.cot.device.agent.common.configuration.Configuration;

import javax.validation.constraints.NotNull;

@ConfigurationPath("agent.services.deviceService")
public class DeviceServiceConfiguration implements Configuration {

    @NotNull
    private Long handlersShutdownTimeout;

	public Long getHandlersShutdownTimeout() {
		return handlersShutdownTimeout;
	}

	public void setHandlersShutdownTimeout(Long handlersShutdownTimeout) {
		this.handlersShutdownTimeout = handlersShutdownTimeout;
	}

	@Override
	public String toString() {
		return DeviceServiceConfiguration.class.getSimpleName() + " [handlersShutdownTimeout=" + handlersShutdownTimeout + "]";
	}
}
