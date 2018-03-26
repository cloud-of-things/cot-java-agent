package com.telekom.cot.device.agent.inventory;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.telekom.cot.device.agent.service.configuration.Configuration;
import com.telekom.cot.device.agent.service.configuration.ConfigurationPath;

@ConfigurationPath("agent.services.inventoryService")
public class InventoryServiceConfiguration implements Configuration {

    @NotNull @NotEmpty
    private String deviceName;
	
    @NotNull @NotEmpty
    private String deviceType;

    @JsonProperty(value="isDevice")
    private boolean device;

    @JsonProperty(value="isAgent")
    private boolean agent;

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public boolean isDevice() {
		return device;
	}

	public void setDevice(boolean device) {
		this.device = device;
	}

	public boolean isAgent() {
		return agent;
	}

	public void setAgent(boolean agent) {
		this.agent = agent;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	@Override
	public String toString() {
		return InventoryServiceConfiguration.class.getSimpleName() + " [deviceName=" + deviceName + ", deviceType="
				+ deviceType + ", device=" + device + ", agent=" + agent + "]";
	}

}
