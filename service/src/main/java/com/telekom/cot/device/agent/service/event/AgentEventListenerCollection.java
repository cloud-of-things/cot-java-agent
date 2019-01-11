package com.telekom.cot.device.agent.service.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentPublishEventException;
import com.telekom.cot.device.agent.common.util.AssertionUtil;

/**
 * Collects all listener.
 *
 */
public class AgentEventListenerCollection {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentEventListenerCollection.class);
    /** The list of handlers. */
    private final Map<Class<? extends AgentEvent>, List<AgentEventListener<AgentEvent>>> listeners = new HashMap<>();

    /**
     * Add a listener.
     * 
     * @param listener
     * @throws AbstractAgentException
     */
    public void add(AgentEventListener<? extends AgentEvent> listener) throws AbstractAgentException {
        LOGGER.info("add listener {}", listener);
        AssertionUtil.assertNotNull(listener, AgentPublishEventException.class, LOGGER, "no listener added");
        AgentEventListenerWrapper wrapper = new AgentEventListenerWrapper(listener);
        if (!listeners.containsKey(wrapper.getEventClass())) {
            listeners.put(wrapper.getEventClass(), new ArrayList<>());
        }
        listeners.get(wrapper.getEventClass()).add(wrapper);
    }

    /**
     * Remove a listener.
     * 
     * @param listener
     */
    public void remove(AgentEventListener<? extends AgentEvent> listener) {
        LOGGER.info("remove listener {}", listener);
        for (Entry<Class<? extends AgentEvent>, List<AgentEventListener<AgentEvent>>> entry : listeners.entrySet()) {
            for (AgentEventListener<AgentEvent> wrapper : entry.getValue()) {
                if (wrapper.equals(listener)) {
                    entry.getValue().remove(wrapper);
                    return;
                }
            }
        }
    }

    /**
     * Get the list of handlers.
     * 
     * @return
     */
    public List<AgentEventListener<AgentEvent>> getListeners(Class<? extends AgentEvent> eventClass) {
        if (!listeners.containsKey(eventClass)) {
            listeners.put(eventClass, new ArrayList<>());
        }
        return listeners.get(eventClass);
    }

    public boolean contains(AgentEventListener<?> listener) {
        return listeners.entrySet().stream().map((e) -> e.getValue()).flatMap((v) -> v.stream())
                        .collect(Collectors.toList()).stream().map((w) -> ((AgentEventListenerWrapper) w).getListener())
                        .collect(Collectors.toList()).contains(listener);
    }

}
