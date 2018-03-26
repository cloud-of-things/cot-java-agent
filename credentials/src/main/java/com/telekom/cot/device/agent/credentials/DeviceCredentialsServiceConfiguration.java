package com.telekom.cot.device.agent.credentials;

import javax.validation.constraints.NotNull;

import com.telekom.cot.device.agent.service.configuration.AgentCredentials;
import com.telekom.cot.device.agent.service.configuration.Configuration;
import com.telekom.cot.device.agent.service.configuration.ConfigurationPath;

@ConfigurationPath("agent.services.deviceCredentialsService")
public class DeviceCredentialsServiceConfiguration implements Configuration{

	public enum DeviceIdTemplates { EXTERNAL_ID_VALUE, HARDWARE_SERIAL }
	
    private int interval;
    
	@NotNull
	private AgentCredentials bootstrapCredentials;
	
	@NotNull
	private DeviceIdTemplates deviceIdTemplate;

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

	public AgentCredentials getBootstrapCredentials() {
		return bootstrapCredentials;
	}

	public void setBootstrapCredentials(AgentCredentials bootstrapCredentials) {
		this.bootstrapCredentials = bootstrapCredentials;
	}

	public DeviceIdTemplates getDeviceIdTemplate() {
		return deviceIdTemplate;
	}

	public void setDeviceIdTemplate(DeviceIdTemplates deviceIdTemplate) {
		this.deviceIdTemplate = deviceIdTemplate;
	}
}
