package com.telekom.cot.device.agent.service.event;

import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.service.event.AgentContext;
import com.telekom.cot.device.agent.service.event.AgentContextImpl;
import com.telekom.cot.device.agent.service.event.AgentEvent;
import com.telekom.cot.device.agent.service.event.AgentEventListener;
import com.telekom.cot.device.agent.service.event.AgentContextImpl.Execution;

public class AgentContextImplTest {

    private List1 list1;
    private TestListener testListener;

    @Before
    public void setUp() {
        list1 = new List1();
        testListener = new TestListener();
    }

    @After
    public void tearDown() {
        assertThat(list1.isExecuted(), Matchers.equalTo(true));
        assertThat(testListener.isExecuted(), Matchers.equalTo(true));
    }

    @Test
    public void testPublisherASYNC() throws AbstractAgentException, InterruptedException {
        AgentContext context = new AgentContextImpl();
        Obj1 obj1 = new Obj1();
        context.addAgentEventListener(list1);
        context.addAgentEventListener(testListener);
        context.getAgentEventPublisher(Execution.ASYNC).publishEvent(obj1);
        context.getAgentEventPublisher(Execution.ASYNC).publishEvent(new TestEvent());
        TimeUnit.MILLISECONDS.sleep(100);
    }

    @Test
    public void testPublisherSYNC() throws AbstractAgentException, InterruptedException {
        AgentContext context = new AgentContextImpl();
        Obj1 obj1 = new Obj1();
        context.addAgentEventListener(list1);
        context.addAgentEventListener(testListener);
        context.getAgentEventPublisher(Execution.SYNC).publishEvent(obj1);
        context.getAgentEventPublisher(Execution.SYNC).publishEvent(new TestEvent());
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
