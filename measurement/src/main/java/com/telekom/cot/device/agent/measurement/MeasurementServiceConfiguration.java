package com.telekom.cot.device.agent.measurement;

import com.telekom.cot.device.agent.common.annotations.ConfigurationPath;
import com.telekom.cot.device.agent.common.configuration.Configuration;

@ConfigurationPath("agent.services.measurementService")
public class MeasurementServiceConfiguration implements Configuration {

	private int sendInterval;

	public int getSendInterval() {
		return sendInterval;
	}

	public void setSendInterval(int sendInterval) {
		this.sendInterval = sendInterval;
	}
}
