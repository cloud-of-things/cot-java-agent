package com.telekom.cot.device.agent.service.event;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.service.event.AgentEvent;
import com.telekom.cot.device.agent.service.event.AgentEventListener;
import com.telekom.cot.device.agent.service.event.AgentEventListenerCollection;

public class AgentEventListenerCollectionTest {

    @Test
    public void testAdd() throws AbstractAgentException {
        AgentEventListenerCollection collection = new AgentEventListenerCollection();
        collection.add(new List1());
        collection.add(new List1());
        assertThat(collection.getListeners(Obj1.class).size(), Matchers.equalTo(2));
    }

    @Test
    public void testRemove() throws AbstractAgentException {
        AgentEventListenerCollection collection = new AgentEventListenerCollection();
        List1 list1 = new List1();
        collection.add(list1);
        assertThat(collection.getListeners(Obj1.class).size(), Matchers.equalTo(1));
        collection.remove(list1);
        assertThat(collection.getListeners(Obj1.class).size(), Matchers.equalTo(0));
    }
    
    @Test
    public void testGet() throws AbstractAgentException {
        AgentEventListenerCollection collection = new AgentEventListenerCollection();
        List1 list1 = new List1();
        collection.add(list1);
        assertThat(collection.getListeners(Obj1.class).size(), Matchers.equalTo(1));
        List<AgentEventListener<AgentEvent>> listeners = collection.getListeners(Obj1.class);
        assertThat(listeners.size(), Matchers.equalTo(1));
    }
    
    @Test
    public void testAddError() {
        AgentEventListenerCollection collection = new AgentEventListenerCollection();
        try {
            collection.add(null);
            fail();
        } catch (AbstractAgentException e) {
            // ignore
        }
    }

    public static class List1 implements AgentEventListener<Obj1> {

        private boolean executed = false;

        @Override
        public void onAgentEvent(Obj1 event) {
            executed = true;
            System.out.println(event.getSource());
        }

        public boolean isExecuted() {
            return executed;
        }
    }

    public static class Obj1 extends AgentEvent {

        @Override
        public Object getSource() {
            return "Obj1";
        }
    }
}
