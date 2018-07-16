package com.telekom.cot.device.agent.platform.mqtt.event;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.telekom.cot.device.agent.platform.mqtt.PublishCallback;
import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;
import com.telekom.cot.device.agent.service.event.AgentEvent;

/**
 * Each event created from the {@code PublishCallback} must be subclassed from this abstract class.
 *
 */
public abstract class PublishedValuesAgentEvent extends AgentEvent {

    /** The event source */
    private Object source;
    /** The published values of the template response. */
    private PublishedValues publishedValues;

    /** The valid constructor. */
    protected PublishedValuesAgentEvent(PublishCallback source, PublishedValues publishedValues) {
        this.source = source;
        this.publishedValues = publishedValues;
    }

    /** The error constructor. */
    protected PublishedValuesAgentEvent(Throwable source, PublishedValues publishedValues) {
        this.source = source;
        this.publishedValues = publishedValues;
    }

    /**
     * Get published values from CoT.
     * 
     * @return the values
     */
    public PublishedValues getPublishedValues() {
        return publishedValues;
    }

    /**
     * Get the PublishCallback source.
     */
    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "PublishedValuesAgentEvent [source=" + source + ", publishedValues=" + publishedValues + "]";
    }

    /**
     * Create a subclassed PublishedValuesAgentEvent.
     * 
     * @param agentEventClass
     *            the event class
     * @param source
     *            the source
     * @param publishedValues
     *            the published values of the template response
     * @return
     */
    public static <T extends PublishedValuesAgentEvent> PublishedValuesAgentEvent createEvent(Class<T> agentEventClass,
                    PublishCallback source, PublishedValues publishedValues) {
        try {
            Constructor<T> constructor = agentEventClass
                            .getConstructor(new Class[] { PublishCallback.class, PublishedValues.class });
            return constructor.newInstance(new Object[] { source, publishedValues });
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException throwable) {
            // TODO write a listener
            return new ErrorPublishValuesAgentEvent(throwable, publishedValues);
        }
    }

    /**
     * The error event. 
     */
    static class ErrorPublishValuesAgentEvent extends PublishedValuesAgentEvent {

        protected ErrorPublishValuesAgentEvent(Throwable throwable, PublishedValues publishedValues) {
            super(throwable, publishedValues);
        }
    }
}
