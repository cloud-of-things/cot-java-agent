package com.telekom.cot.device.agent.service.event;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.service.event.AgentContextImpl.Execution;

/**
 * The context handles common functionality like:
 * <ul>
 * <li>get a publisher</li>
 * <li>add a listener</li>
 * </ul>
 *
 */
public interface AgentContext {

    /**
     * Get a sync or async publisher.
     * 
     * @param execution
     *            the publisher is able to execute the listener SYNC or ASYNC
     * @return the publisher interface
     * @throws AbstractAgentException
     *             if the execution is declared but not implemented
     */
    public AgentEventPublisher getAgentEventPublisher(Execution execution) throws AbstractAgentException;

    /**
     * Add a listener.
     * 
     * @param listener
     *            the listener implementation
     * @throws AbstractAgentException
     *             if the listener is null
     */
    public void addAgentEventListener(AgentEventListener<?> listener) throws AbstractAgentException;

    /**
     * Remove a listener.
     * 
     * @param listener
     */
    public void removeAgentEventListener(AgentEventListener<?> listener);

    public boolean containsAgentEventListener(AgentEventListener<?> listener);
}
