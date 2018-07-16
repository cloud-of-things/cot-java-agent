package com.telekom.cot.device.agent.service;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.service.event.AgentContext;

/**
 * {@link AgentService} is the base interface for all services in the CoT Java Reference Agent
 *
 */
public interface AgentService {

	/**
	 * initializes the service
	 * @param serviceProvider service provider instance to get other services
	 * @param agentCredentialsManager agent credentials manager instance to get and set agent credentials
	 * @param agentContext handles common functionality like: publisher, listeners, etc.
	 * @throws AbstractAgentException if an error occurs during initialization 
	 */
	public void init(AgentContext agentContext) throws AbstractAgentException;
	
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
	
	/**
	 * gets the package of the class implements the {@link AgentService} interface
	 * @return the package of this interface's implementation
	 */
	public default Package getPackage() {
	    return this.getClass().getPackage();
	}
}
