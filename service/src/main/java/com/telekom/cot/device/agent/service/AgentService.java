package com.telekom.cot.device.agent.service;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.service.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.service.configuration.ConfigurationManager;

/**
 * {@link AgentService} is the base interface for all services in the CoT Java Reference Agent
 *
 */
public interface AgentService {

	/**
	 * initializes the service
	 * @param serviceProvider service provider instance to get other services
	 * @param configurationManager configuration manager instance to request and update configurations
	 * @param agentCredentialsManager agent credentials manager instance to get and set agent credentials
	 * @throws AbstractAgentException if an error occurs during initialization 
	 */
	public void init(AgentServiceProvider serviceProvider, ConfigurationManager configurationManager, AgentCredentialsManager agentCredentialsManager) throws AbstractAgentException;
	
	/**
	 * starts the service
	 */
	public void start() throws AbstractAgentException;
	
	/**
	 * stops the service and cleans it up
	 */
	public void stop() throws AbstractAgentException;

	/**
	 * returns whether the service is started or not
	 */
	public boolean isStarted();
}
