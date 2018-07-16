package com.telekom.cot.device.agent.service.event;

import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;

public class AgentEventUtilTest {

    @Test
    public void testGetEventTypeByGenericSuperclass() {
        SubBaseListener subListener = new SubBaseListener();
        Class<? extends AgentEvent> listenerEventType = AgentEventUtil.getEventTypeByGenericSuperclass(subListener);
        assertThat(listenerEventType, Matchers.equalTo(TestAgentEvent.class));
        //
        SubMainListener mainListener = new SubMainListener();
        listenerEventType = AgentEventUtil.getEventTypeByGenericSuperclass(mainListener);
        assertThat(listenerEventType, Matchers.equalTo(TestAgentEvent.class));
        //
        listenerEventType = AgentEventUtil.getEventTypeByGenericInterface(subListener);
        assertThat(listenerEventType, Matchers.equalTo(null));
        // getEventType
        listenerEventType = AgentEventUtil.getEventType(subListener);
        assertThat(listenerEventType, Matchers.equalTo(TestAgentEvent.class));
        listenerEventType = AgentEventUtil.getEventType(mainListener);
        assertThat(listenerEventType, Matchers.equalTo(TestAgentEvent.class));    
    }

    @Test
    public void testGetEventTypeByGenericInterface() {
        // getEventTypeByGenericInterface
        SimpleGenericInterfaceImpl simpleGenericInterfaceImpl = new SimpleGenericInterfaceImpl();
        Class<? extends AgentEvent> listenerEventType = AgentEventUtil
                        .getEventTypeByGenericInterface(simpleGenericInterfaceImpl);
        assertThat(listenerEventType, Matchers.equalTo(SimpleEvent.class));
        // getEventTypeByGenericInterface
        SubGenericInterfaceImpl subGenericInterfaceImpl = new SubGenericInterfaceImpl();
        listenerEventType = AgentEventUtil.getEventTypeByGenericInterface(subGenericInterfaceImpl);
        assertThat(listenerEventType, Matchers.equalTo(SimpleEvent.class));
        // getEventType
        listenerEventType = AgentEventUtil.getEventType(simpleGenericInterfaceImpl);
        assertThat(listenerEventType, Matchers.equalTo(SimpleEvent.class));
        listenerEventType = AgentEventUtil.getEventType(subGenericInterfaceImpl);
        assertThat(listenerEventType, Matchers.equalTo(SimpleEvent.class));        
    }

    /*
     * GenericInterface
     */
    static class SubGenericInterfaceImpl extends SimpleGenericInterfaceImpl
                    implements FirstGenericInterfaces<Object, Object>, SecondGenericInterfaces<Object> {

        @Override
        public void onAgentEvent(SimpleEvent event) {
        }
    }

    static class SimpleGenericInterfaceImpl implements AgentEventListener<SimpleEvent> {

        @Override
        public void onAgentEvent(SimpleEvent event) {
        }
    }

    static class SimpleEvent extends AgentEvent {

        @Override
        public Object getSource() {
            return null;
        }
    }

    /*
     * GenericSuperclass
     */
    static class SubBaseListener extends BaseListener<TestAgentEvent> {

        @Override
        public void onAgentEvent(TestAgentEvent event) {
        }
    }

    static class SubMainListener extends MainListener<TestAgentEvent, Object, Object> {

        @Override
        public void onAgentEvent(TestAgentEvent event) {
        }
    }

    static abstract class MainListener<E extends AgentEvent, T, X> extends BaseListener<E>
                    implements FirstGenericInterfaces<T, X> {
    }

    static abstract class BaseListener<E extends AgentEvent> implements AgentEventListener<E> {
    }

    static abstract class TestAgentEvent extends BaseAgentEvent {
    }

    static abstract class BaseAgentEvent extends AgentEvent {

        @Override
        public Object getSource() {
            return null;
        }
    }

    interface FirstGenericInterfaces<T, X> {
    }

    interface SecondGenericInterfaces<T> {
    }
}
