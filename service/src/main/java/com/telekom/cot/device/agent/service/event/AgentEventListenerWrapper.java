package com.telekom.cot.device.agent.service.event;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.telekom.cot.device.agent.service.event.AgentEventUtil.*;

/**
 * The handler of a listener.
 *
 */
public class AgentEventListenerWrapper implements AgentEventListener<AgentEvent> {

    /** The Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentEventListenerWrapper.class);
    /** The wrapped listener. */
    private AgentEventListener<? extends AgentEvent> listener;
    /** The class of the listener type. */
    private Class<? extends AgentEvent> eventClass;

    public AgentEventListenerWrapper(AgentEventListener<? extends AgentEvent> listener) {
        this.listener = listener;
        this.eventClass = getEventType(listener);
        if (Objects.isNull(this.eventClass)) {
            LOGGER.error("event type could not be found");
        }
    }

    AgentEventListener<? extends AgentEvent> getListener() {
        return listener;
    }

    Class<? extends AgentEvent> getEventClass() {
        return eventClass;
    }

    /**
     * Invoke the event by reflection.
     * 
     * @param event
     *            the given event
     * @return true, if the 'onAgentEvent' is called
     */
    public void onAgentEvent(AgentEvent event) {
        if (isEventType(event)) {
            LOGGER.info("onAgentEvent event={}", event);
            try {
                invoke(listener, event);
            } catch (ReflectiveOperationException e) {
                LOGGER.error("can't invoke event " + event, e);
            }
        } else {
            LOGGER.error("can't invoke event");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (Objects.isNull(obj)) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (getListener() == obj) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Is the event type equals the listener type.
     * 
     * @param event
     *            the given event
     * @return true, if the types are equal
     */
    private boolean isEventType(AgentEvent event) {
        boolean result = Objects.nonNull(event) && Objects.nonNull(this.eventClass);
        result = result && eventClass == event.getClass();
        LOGGER.info("is the event type equals listener type result={}", result);
        return result;
    }
}