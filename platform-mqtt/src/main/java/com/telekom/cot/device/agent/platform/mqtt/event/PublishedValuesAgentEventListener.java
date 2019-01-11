package com.telekom.cot.device.agent.platform.mqtt.event;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;
import com.telekom.cot.device.agent.service.event.AgentEventListener;

/**
 * Each listener who is listen of a template response must be subclassed from this class.
 * 
 * @param <T>
 *            the instance which is created from the PublishedValues
 * @param <E>
 *            the instance of event
 */
public abstract class PublishedValuesAgentEventListener<T, E extends PublishedValuesAgentEvent>
                implements AgentEventListener<E> {

    /** Hold the published values in a eventBusinessObject. */
    private Optional<E> optionalEvent = Optional.empty();
    private T businessObject;
    private Consumer<T> callback;

    /** Create an optional of the event */
    @Override
    public void onAgentEvent(E event) {
        synchronized (optionalEvent) {
            optionalEvent = Optional.of(event);
            if (optionalEvent.isPresent()) {
                businessObject = create(optionalEvent.get().getPublishedValues());
                if (Objects.nonNull(businessObject)) {
                    handle(businessObject);
                    if (Objects.nonNull(callback)) {
                        callback.accept(businessObject);
                    }
                }
            }
        }
    }

    void handle(T businessObject) {
    }

    /**
     * Callback of event.
     * 
     * @param callback
     */
    public void provideEvent(Consumer<T> callback) {
        this.callback = callback;
    }

    /**
     * Wait on an event and create the suitable business object based on the PublishedValues.
     */
    public T waitOnAgentEventAndCreate() throws InterruptedException {
        optionalEvent = Optional.empty();
        while (true) {
            synchronized (optionalEvent) {
                if (optionalEvent.isPresent()) {
                    break;
                }
            }
            TimeUnit.MILLISECONDS.sleep(500);
        }
        return businessObject;
    }

    /**
     * Create the business object.
     *
     * @return the business object
     */
    public abstract T create(PublishedValues publishedValues);

    public abstract Class<E> getEventClass();
}
