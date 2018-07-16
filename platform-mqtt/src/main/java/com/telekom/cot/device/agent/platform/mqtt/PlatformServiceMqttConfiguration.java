package com.telekom.cot.device.agent.platform.mqtt;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.telekom.cot.device.agent.common.annotations.ConfigurationPath;
import com.telekom.cot.device.agent.platform.PlatformServiceConfiguration;

@ConfigurationPath("agent.services.platformService")
public class PlatformServiceMqttConfiguration extends PlatformServiceConfiguration {

	@NotNull
	@Valid
	private MqttConfiguration mqttConfiguration;

	public MqttConfiguration getMqttConfiguration() {
		return mqttConfiguration;
	}

	public void setMqttConfiguration(MqttConfiguration mqttConfiguration) {
		this.mqttConfiguration = mqttConfiguration;
	}

	@Override
	public String toString() {
		return PlatformServiceMqttConfiguration.class.getSimpleName() + " [hostName=" + getHostName() + ", externalId="
				+ getExternalIdConfig() + ", mqttConfiguration=" + mqttConfiguration + "]";
	}

	public static class MqttConfiguration {
		@NotNull
		@NotEmpty
		private String port;

		@NotNull
		@NotEmpty
		private String xId;

		@NotNull
		@Min(1)
		private Integer timeout;

		@NotNull
		@Min(1)
		private Integer delaySendMeasurement;

		public String getPort() {
			return port;
		}

		public void setPort(String port) {
			this.port = port;
		}

		public String getxId() {
			return xId;
		}

		public void setxId(String xId) {
			this.xId = xId;
		}

		public Integer getTimeout() {
			return timeout;
		}

		public void setTimeout(Integer timeout) {
			this.timeout = timeout;
		}

		public Integer getDelaySendMeasurement() {
			return delaySendMeasurement;
		}

		public void setDelaySendMeasurement(Integer delaySendMeasurement) {
			this.delaySendMeasurement = delaySendMeasurement;
		}

		@Override
		public String toString() {
			return MqttConfiguration.class.getSimpleName() + " [port=" + port + ", xId=" + xId + ", timeout=" + timeout
					+ ", delaySendMeasurement=" + delaySendMeasurement + "]";
		}
	}
}
