package com.telekom.cot.device.agent.service.event;

/**
 * Publish a concrete event. <br>
 * The publisher can be async or sync.
 * <ul>
 * Async Publisher:
 * <li>execute all listener in a thread</li>
 * <li>these threads are queued by fixed size</li>
 * <li>the caller is not blocked</li>
 * </ul>
 * <ul>
 * Sync Publisher:
 * <li>execute all listener sequentially</li>
 * <li>the caller is blocked</li>
 * </ul>
 *
 */
public interface AgentEventPublisher {

    /**
     * Publish an agent event.
     * 
     * @param event
     */
    public void publishEvent(AgentEvent event);
}
