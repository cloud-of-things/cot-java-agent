package com.telekom.cot.device.agent.service;

import java.util.List;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;

/**
 * {@link AgentServiceProvider} provides methods to get one ore more AgentService(s)
 * (from {@link AgentServiceManagerImpl})
 *
 */
public interface AgentServiceProvider {

	/**
	 * gets an agent service of given service type
	 * @param <T> type of the agent service to get
	 * @param serviceType type representation of the agent service to get
	 * @return an agent service of given type
	 * @throws AbstractAgentException if not exactly one service of given type found
	 */
	public <T extends AgentService> T getService(Class<T> serviceType) throws AbstractAgentException;

	/**
	 * gets a list of agent services of given service type
	 * @param <T> type of the agent service to get
	 * @param serviceType type representation of the agent service to get
	 * @return a list of agent services of given type
	 * @throws AbstractAgentException if no services of given type found 
	 */
	public <T extends AgentService> List<T> getServices(Class<T> serviceType) throws AbstractAgentException;
}
