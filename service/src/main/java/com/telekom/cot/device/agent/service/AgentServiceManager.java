package com.telekom.cot.device.agent.service;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.service.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.service.configuration.ConfigurationManager;

public interface AgentServiceManager extends AgentServiceProvider {

	/**
	 * loads and initializes all agent services from existing packages at class path
	 * @param configurationManager a valid instance of {@link ConfigurationManager} used for initialization ("agent.yaml")
	 * @param agentCredentialsManager a valid instance of {@link AgentCredentialsManager} used for initialization
	 * @throws AbstractAgentException
	 */
	public void loadAndInitServices(ConfigurationManager configurationManager, AgentCredentialsManager agentCredentialsManager) throws AbstractAgentException;
	
	/**
	 * gets the count of all loaded services
	 * @return count of loaded services
	 */
	public int count();

}
