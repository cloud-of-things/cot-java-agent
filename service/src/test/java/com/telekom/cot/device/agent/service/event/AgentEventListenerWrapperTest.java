package com.telekom.cot.device.agent.service.event;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.service.event.AgentEvent;
import com.telekom.cot.device.agent.service.event.AgentEventListener;
import com.telekom.cot.device.agent.service.event.AgentEventListenerWrapper;

public class AgentEventListenerWrapperTest {

    private Logger mockLogger;

    @Before
    public void setUp() {
        mockLogger = Mockito.mock(Logger.class);
    }

    @Test
    public void testGenerics() {
        List3 list3 = new List3();
        Obj5 obj5 = new Obj5();
        AgentEventListenerWrapper handler3 = new AgentEventListenerWrapper(list3);
        handler3.onAgentEvent(obj5);
    }

    @SuppressWarnings("unused")
    @Test
    public void testIsEventType() {
        // listeners
        List1 list1 = new List1();
        List2 list2 = new List2();
        // events
        Obj1 obj1 = new Obj1();
        Obj2 obj2 = new Obj2();
        // handlers
        AgentEventListenerWrapper handler1 = (AgentEventListenerWrapper) new AgentEventListenerWrapper(list1);
        AgentEventListenerWrapper handler2 = (AgentEventListenerWrapper) new AgentEventListenerWrapper(list2);
        // tests
        assertThat(handler1.getEventClass(), Matchers.equalTo(Obj1.class));
        assertThat(handler2.getEventClass(), Matchers.equalTo(Obj2.class));
    }

    @Test
    public void testOnAgentEvent() {
        // listeners
        List1 list1 = new List1();
        List2 list2 = new List2();
        // events
        Obj1 obj1 = new Obj1();
        Obj2 obj2 = new Obj2();
        // handlers
        AgentEventListenerWrapper handler1 = (AgentEventListenerWrapper) new AgentEventListenerWrapper(list1);
        AgentEventListenerWrapper handler2 = (AgentEventListenerWrapper) new AgentEventListenerWrapper(list2);
        // tests
        assertThat(handler1.getEventClass(), Matchers.equalTo(Obj1.class));
        assertThat(handler2.getEventClass(), Matchers.equalTo(Obj2.class));
        // inject
        InjectionUtil.inject(handler1, mockLogger);
        handler1.onAgentEvent(obj1);
        // inject
        InjectionUtil.inject(handler2, mockLogger);
        handler2.onAgentEvent(obj2);
        verify(mockLogger, times(2)).info("is the event type equals listener type result={}", true);
        verify(mockLogger).info("onAgentEvent event={}", obj1);
        verify(mockLogger).info("onAgentEvent event={}", obj2);
        handler2.onAgentEvent(obj1);
        verify(mockLogger).info("is the event type equals listener type result={}", false);
        verify(mockLogger).error("can't invoke event");
    }

    public static class List1 implements AgentEventListener<Obj1> {

        @Override
        public void onAgentEvent(Obj1 event) {
            System.out.println(event.getSource());
        }
    }

    public static class Obj1 extends AgentEvent {

        @Override
        public Object getSource() {
            return "Obj1";
        }

        @Override
        public String toString() {
            return "Obj1";
        }
    }

    public static class List2 implements AgentEventListener<Obj2> {

        @Override
        public void onAgentEvent(Obj2 event) {
            System.out.println(event.getSource());
        }
    }

    public static class Obj2 extends AgentEvent {

        @Override
        public Object getSource() {
            return "Obj2";
        }

        @Override
        public String toString() {
            return "Obj2";
        }
    }

    public static class List3 extends List4 {

        @Override
        public void onAgentEvent(Obj5 event) {
            System.out.println(event.getSource());
        }
    }

    public static abstract class List4 extends List5<Obj5> {
    }

    public static abstract class List5<E extends AgentEvent> implements AgentEventListener<E> {
    }

    public static class Obj5 extends Obj6 {
    }

    public static class Obj6 extends AgentEvent {

        @Override
        public Object getSource() {
            return "Obj6";
        }

        @Override
        public String toString() {
            return "Obj6";
        }
    }
}
