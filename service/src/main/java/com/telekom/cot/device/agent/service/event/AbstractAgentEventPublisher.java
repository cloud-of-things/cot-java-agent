package com.telekom.cot.device.agent.service.event;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The abstract publisher is able to handle the execution by the collection of {@codeAgentEventListenerHandler}.
 *
 */
public abstract class AbstractAgentEventPublisher implements AgentEventPublisher {

    /** The Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAgentEventPublisher.class);
    /** The handler collection. */
    private final AgentEventListenerCollection collection;

    public AbstractAgentEventPublisher(AgentEventListenerCollection collection) {
        LOGGER.info("create publisher");
        this.collection = collection;
    }

    /** Must be implemented. */
    public abstract void publishEvent(AgentEvent event);

    /** Get the listener wrapped by the handler. */
    protected List<AgentEventListener<AgentEvent>> getListeners(AgentEvent agentEvent) {
        return collection.getListeners(agentEvent.getClass());
    }
}
