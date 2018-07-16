package com.telekom.cot.device.agent.service.event;

import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.service.event.AbstractAgentEventPublisher;
import com.telekom.cot.device.agent.service.event.AgentEvent;
import com.telekom.cot.device.agent.service.event.AgentEventListener;
import com.telekom.cot.device.agent.service.event.AgentEventListenerCollection;

public class AbstractAgentEventPublisherTest {

    private static Boolean executed = Boolean.FALSE;

    @Test
    public void testCollection() throws AbstractAgentException {
        Obj1 obj1 = new Obj1();
        AgentEventListenerCollection collection = new AgentEventListenerCollection();
        AbstractAgentEventPublisher publisher = new AbstractAgentEventPublisher(collection) {

            @Override
            public void publishEvent(AgentEvent event) {
                executed = Boolean.TRUE;
                assertThat(getListeners(obj1).size(), Matchers.equalTo(1));
            }
        };
        collection.add(new List1());
        publisher.publishEvent(obj1);
        assertThat(executed, Matchers.equalTo(true));
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
    }
}
