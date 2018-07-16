package com.telekom.cot.device.agent.service.event;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Asynchronous agent event publisher handles each listener implementation by an thread. These threads are queued in a
 * fixed thread pool.
 *
 */
public class AgentEventPublisherAsync extends AbstractAgentEventPublisher {

    /** The Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentEventPublisherAsync.class);
    /** The thread pool who handles the listener threads by a specified size. */
    private ExecutorService executorService = null;

    public AgentEventPublisherAsync(AgentEventListenerCollection collection, int threadPoolSize) {
        super(collection);
        executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    @Override
    public void publishEvent(AgentEvent event) {
        LOGGER.info("publish event by source {}", event.getSource());
        // loop throw all handlers
        for (AgentEventListener<AgentEvent> listener : getListeners(event)) {
            LOGGER.info("submit event by listener {}", event);
            // add a callable to the thread pool
            executorService.submit(Executors.callable(() -> {
                listener.onAgentEvent(event);
            }));
        }
    }
}
