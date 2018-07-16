package com.telekom.cot.device.agent.common.configuration;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;

public interface AgentCredentialsManager {

	/**
	 * gets the current used agent credentials (from credentials file or temporary set)
	 * @return the current used agent credentials
	 * @throws AbstractAgentException if there are no agent credentials 
	 */
	public AgentCredentials getCredentials() throws AbstractAgentException;

	/**
	 * sets the current used agent credentials temporary (not persisted in credentials file) 
	 */
	public void setCredentials(AgentCredentials credentials);

	/**
	 * reads the agent credentials from credentials file and sets them as current credentials
	 * @return the read agent credentials
	 * @throws AbstractAgentException if agent credentials can't be read from file 
	 */
	public AgentCredentials readCredentials() throws AbstractAgentException;

	/**
	 * writes the agent credentials to credentials file and sets them as current used agent credentials
	 * @throws AbstractAgentException if agent credentials can't be write to file 
	 */
	public void writeCredentials(AgentCredentials credentials) throws AbstractAgentException;

}
