package com.telekom.cot.device.agent.operation;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.telekom.cot.device.agent.common.annotations.ConfigurationPath;
import com.telekom.cot.device.agent.common.configuration.Configuration;

@ConfigurationPath("agent.services.operationService")
public class OperationServiceConfiguration implements Configuration{

    @NotNull @Positive
	private Integer interval;
    @NotNull @Positive
	private Integer shutdownTimeout;
    @NotNull @Positive
	private Integer handlersShutdownTimeout;
    
	public Integer getInterval() {
		return interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	public Integer getShutdownTimeout() {
		return shutdownTimeout;
	}

	public void setShutdownTimeout(Integer shutdownTimeout) {
		this.shutdownTimeout = shutdownTimeout;
	}

	public Integer getHandlersShutdownTimeout() {
		return handlersShutdownTimeout;
	}

	public void setHandlersShutdownTimeout(Integer handlersShutdownTimeout) {
		this.handlersShutdownTimeout = handlersShutdownTimeout;
	}

	@Override
	public String toString() {
		return OperationServiceConfiguration.class.getSimpleName() + " [interval=" + interval
				+ ", shutdownTimeout=" + shutdownTimeout + ", handlersShutdownTimeout=" + handlersShutdownTimeout + "]";
	}

}
