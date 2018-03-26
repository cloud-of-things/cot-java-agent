package com.telekom.cot.device.agent.service.configuration.testconfigs;

import com.telekom.cot.device.agent.service.configuration.Configuration;

public class ConfigurationNoAnnotation implements Configuration {

	private AgentConfigPart agent = new AgentConfigPart();
	
	public AgentConfigPart getAgent() {
		return agent;
	}

	public void setAgent(AgentConfigPart agent) {
		this.agent = agent;
	}
}
