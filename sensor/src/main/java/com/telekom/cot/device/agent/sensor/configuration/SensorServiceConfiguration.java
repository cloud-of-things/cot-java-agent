package com.telekom.cot.device.agent.sensor.configuration;

import com.telekom.cot.device.agent.common.annotations.ConfigurationPath;
import com.telekom.cot.device.agent.common.configuration.Configuration;

@ConfigurationPath("agent.services.sensorService")
public class SensorServiceConfiguration implements Configuration {

	private int sendInterval;

	public int getSendInterval() {
		return sendInterval;
	}

	public void setSendInterval(int sendInterval) {
		this.sendInterval = sendInterval;
	}
}
