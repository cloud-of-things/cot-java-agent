package com.telekom.cot.device.agent.service.event;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentEventUtil {

    /** The Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentEventUtil.class);
    /** The listener method (see {@code AgentEventListener}. */
    private static final String ON_AGENT_EVENT = "onAgentEvent";

    /**
     * Get the event type from the listener.
     * 
     * @return type or null
     * @throws ClassNotFoundException
     */
    public static Class<? extends AgentEvent> getEventType(AgentEventListener<? extends AgentEvent> listener) {
        LOGGER.info("get the event type of listener {}", listener);
        Class<? extends AgentEvent> eventClass = null;
        eventClass = AgentEventUtil.getEventTypeByGenericSuperclass(listener);
        eventClass = Objects.isNull(eventClass) ? AgentEventUtil.getEventTypeByGenericInterface(listener) : eventClass;
        LOGGER.info("the event type of listener is {}", eventClass);
        return eventClass;
    }

    /**
     * Invoke the method onAgentEvent of the listener.
     * 
     * @param listener
     *            the agent event listener
     * @param event
     *            the event
     * @throws ReflectiveOperationException
     */
    public static void invoke(AgentEventListener<? extends AgentEvent> listener, AgentEvent event)
                    throws ReflectiveOperationException {
        LOGGER.info("invoke event={}", event);
        Class<?>[] params = new Class[] { AgentEvent.class };
        Method method = listener.getClass().getMethod(ON_AGENT_EVENT, params);
        method.invoke(listener, event);
    }

    /**
     * Get the event type from the listener by the GenericSuperclass.
     * 
     * @param listener
     *            the agent event listener
     * @return the event class
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends AgentEvent> getEventTypeByGenericSuperclass(
                    AgentEventListener<? extends AgentEvent> listener) {
        Class<?> genericSuperclass = listener.getClass();
        Class<? extends AgentEvent> typeClass = null;
        boolean continueSearch = true;
        while (continueSearch) {
            Type genericSuperclassType = genericSuperclass.getGenericSuperclass();
            if (Objects.isNull(genericSuperclassType)) {
                break;
            } else if (!(genericSuperclassType instanceof ParameterizedType)) {
                genericSuperclass = genericSuperclass.getSuperclass();
                continue;
            }
            ParameterizedType parameterizedType = (ParameterizedType) genericSuperclassType;
            for (Type genericType : parameterizedType.getActualTypeArguments()) {
                Class<?> clazz;
                try {
                    clazz = Class.forName(genericType.getTypeName());
                } catch (ClassNotFoundException e) {
                    continueSearch = false;
                    break;
                }
                if (isType(clazz, AgentEvent.class)) {
                    typeClass = (Class<? extends AgentEvent>) clazz;
                    continueSearch = false;
                    break;
                }
            }
        }
        return typeClass;
    }

    /**
     * Get the event type from the listener by the GenericInterfaces.
     * 
     * @param listener
     *            the agent event listener
     * @return the event class
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends AgentEvent> getEventTypeByGenericInterface(
                    AgentEventListener<? extends AgentEvent> listener) {
        Class<?> genericInterfaces = listener.getClass();
        Class<? extends AgentEvent> typeClass = null;
        boolean continueSearch = true;
        while (continueSearch) {
            Type[] genericInterfacesTypes = genericInterfaces.getGenericInterfaces();
            if (genericInterfacesTypes.length == 0) {
                genericInterfaces = genericInterfaces.getSuperclass();
                continue;
            }
            // found a generic interfaces
            for (Type genericInterface : genericInterfacesTypes) {
                if (genericInterface instanceof ParameterizedType) {
                    ParameterizedType genericInterfaceType = (ParameterizedType) genericInterface;
                    Class<?> agentEventListenerClass;
                    try {
                        agentEventListenerClass = Class.forName(genericInterfaceType.getRawType().getTypeName());
                    } catch (ClassNotFoundException e1) {
                        continueSearch = false;
                        break;
                    }
                    if (!isType(agentEventListenerClass, AgentEventListener.class)) {
                        continue;
                    }
                    // agentEventListenerClass is a AgentEventListener
                    Type[] types = genericInterfaceType.getActualTypeArguments();
                    if (Objects.nonNull(types) && types.length == 1) {
                        Class<?> clazz;
                        try {
                            clazz = Class.forName(types[0].getTypeName());
                        } catch (ClassNotFoundException e) {
                            continueSearch = false;
                            break;
                        }
                        if (isType(clazz, AgentEvent.class)) {
                            // found event
                            typeClass = (Class<? extends AgentEvent>) clazz;
                            continueSearch = false;
                            break;
                        }
                    } else {
                        // there are more than one types
                        continueSearch = false;
                        break;
                    }
                }
            }
            if (continueSearch) {
                genericInterfaces = genericInterfaces.getSuperclass();
            }
        }
        return typeClass;
    }

    private static boolean isType(Class<?> subclass, Class<?> clazz) {
        while (Objects.nonNull(subclass) && !subclass.equals(Object.class)) {
            if (subclass.equals(clazz)) {
                return true;
            }
            subclass = subclass.getSuperclass();
        }
        return false;
    }
}
