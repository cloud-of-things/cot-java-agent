package com.telekom.cot.device.agent.service.event;

/**
 * The listener of a specified event.
 * 
 * @param <T>
 */
public interface AgentEventListener<E extends AgentEvent> {

    /**
     * Handle this event by concrete implementation.
     * 
     * @param event
     */
    public void onAgentEvent(E event);
}
