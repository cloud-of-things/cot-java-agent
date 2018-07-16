package com.telekom.cot.device.agent.credentials;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.telekom.cot.device.agent.common.annotations.ConfigurationPath;
import com.telekom.cot.device.agent.common.configuration.AgentCredentials;
import com.telekom.cot.device.agent.common.configuration.Configuration;

@ConfigurationPath("agent.services.deviceCredentialsService")
public class DeviceCredentialsServiceConfiguration implements Configuration{

	public enum DeviceIdTemplates { EXTERNAL_ID_VALUE, HARDWARE_SERIAL }
	
	@NotNull
    private Integer interval;
    @NotNull @Valid
    private AgentCredentials bootstrapCredentials;
    
	@NotNull
	private DeviceIdTemplates deviceIdTemplate;

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

	public DeviceIdTemplates getDeviceIdTemplate() {
		return deviceIdTemplate;
	}

	public void setDeviceIdTemplate(DeviceIdTemplates deviceIdTemplate) {
		this.deviceIdTemplate = deviceIdTemplate;
	}
	
    public AgentCredentials getBootstrapCredentials() {
        bootstrapCredentials.setBootstrappingMode(true);
        return bootstrapCredentials;
    }

    public void setBootstrapCredentials(AgentCredentials bootstrapCredentials) {
        this.bootstrapCredentials = bootstrapCredentials;
    }	
}
