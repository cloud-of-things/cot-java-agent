package com.telekom.cot.device.agent.service.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentPublishEventException;

/**
 * The common application context in all services.
 */
public class AgentContextImpl implements AgentContext {

    /** The Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentContextImpl.class);
    /** The agent listener collection. */
    private final AgentEventListenerCollection collection = new AgentEventListenerCollection();
    /** The async publisher. */
    private final AbstractAgentEventPublisher agentEventPublisherAsync;
    /** The sync publisher. */
    private final AbstractAgentEventPublisher agentEventPublisherSync;

    /** The publisher execution. */
    public enum Execution {
        SYNC, ASYNC
    }

    /** The public constructor creates an sync and async publisher. */ 
    public AgentContextImpl() {
        agentEventPublisherAsync = new AgentEventPublisherAsync(collection, 5);
        agentEventPublisherSync = new AgentEventPublisherSync(collection);
    }

    /** Add a listener. */
    @Override
    public void addAgentEventListener(AgentEventListener<?> listener) throws AbstractAgentException {
        collection.add(listener);
    }

    /** Get a publisher. */
    @Override
    public AgentEventPublisher getAgentEventPublisher(Execution execution) throws AbstractAgentException {
        if (Execution.ASYNC == execution) {
            return agentEventPublisherAsync;
        } else if (Execution.SYNC == execution) {
            return agentEventPublisherSync;
        } else {
            LOGGER.error("can't get the suitable publisher by {}", execution);
            throw new AgentPublishEventException("can't get the suitable publisher");
        }
    }
    
    /** Remove a publisher. */
    @Override
    public void removeAgentEventListener(AgentEventListener<?> listener) {
        collection.remove(listener);
    }
    
    @Override
    public boolean containsAgentEventListener(AgentEventListener<?> listener) {
        return collection.contains(listener);
    }
}
