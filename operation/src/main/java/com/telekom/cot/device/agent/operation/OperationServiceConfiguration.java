package com.telekom.cot.device.agent.operation;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.telekom.cot.device.agent.service.configuration.Configuration;
import com.telekom.cot.device.agent.service.configuration.ConfigurationPath;

@ConfigurationPath("agent.services.operationService")
public class OperationServiceConfiguration implements Configuration{

    @NotNull @Positive
	private Integer interval;
    @NotNull @Positive
	private Integer resultSize;
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

	public Integer getResultSize() {
		return resultSize;
	}

	public void setResultSize(Integer resultSize) {
		this.resultSize = resultSize;
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
		return OperationServiceConfiguration.class.getSimpleName() + " [interval=" + interval + ", resultSize=" + resultSize
				+ ", shutdownTimeout=" + shutdownTimeout + ", handlersShutdownTimeout=" + handlersShutdownTimeout + "]";
	}

}
