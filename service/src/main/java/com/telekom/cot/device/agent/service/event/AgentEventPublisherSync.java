package com.telekom.cot.device.agent.service.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronous agent event publisher handles each listener implementation sequentially and blocks the caller.
 *
 */
public class AgentEventPublisherSync extends AbstractAgentEventPublisher {

    /** The Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentEventPublisherSync.class);

    public AgentEventPublisherSync(AgentEventListenerCollection collection) {
        super(collection);
    }

    @Override
    public void publishEvent(AgentEvent event) {
        LOGGER.info("publish event by source {}", event.getSource());
        // loop throw all handlers
        for (AgentEventListener<AgentEvent> listener : getListeners(event)) {
            LOGGER.info("handle event by listener {}", event);
            // add a callable to the thread pool
            listener.onAgentEvent(event);
        }
    }
}
