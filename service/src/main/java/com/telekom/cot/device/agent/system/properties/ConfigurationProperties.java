package com.telekom.cot.device.agent.system.properties;

public class ConfigurationProperties implements Properties {

	private String config;

	public ConfigurationProperties() {
	}
	
	public ConfigurationProperties(String config) {
		this.config = config;
	}
	
	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	@Override
	public String toString() {
		return ConfigurationProperties.class.getSimpleName() + " [config=" + config + "]";
	}

}
