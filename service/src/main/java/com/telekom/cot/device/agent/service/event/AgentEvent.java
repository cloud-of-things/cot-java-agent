package com.telekom.cot.device.agent.service.event;

/**
 * The abstract agent event. 
 * This is used from publisher. 
 * The listener must specify a concrete subclass. 
 *
 */
public abstract class AgentEvent {

    /** The source of the event */
    public abstract Object getSource();
}
