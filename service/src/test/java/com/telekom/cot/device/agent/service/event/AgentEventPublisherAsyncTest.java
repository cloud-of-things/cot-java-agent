package com.telekom.cot.device.agent.service.event;

import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.service.event.AgentEvent;
import com.telekom.cot.device.agent.service.event.AgentEventListener;
import com.telekom.cot.device.agent.service.event.AgentEventListenerCollection;
import com.telekom.cot.device.agent.service.event.AgentEventPublisherAsync;

public class AgentEventPublisherAsyncTest {

    @Test
    public void testPublishEventPoolSizeByOne() throws InterruptedException, AbstractAgentException {
        AgentEventListenerCollection collection = new AgentEventListenerCollection();
        AgentEventPublisherAsync async = new AgentEventPublisherAsync(collection, 1);
        List1 list1 = new List1();
        Obj1 obj11 = new Obj1();
        Obj1 obj12 = new Obj1();
        collection.add(list1);
        // publish three events
        async.publishEvent(obj11);
        async.publishEvent(obj12);
        async.publishEvent(new Obj2());
        // wait
        TimeUnit.SECONDS.sleep(5);
        // only two events are expected
        assertThat(list1.getEvents().size(), org.hamcrest.Matchers.equalTo(2));
        // check the first type
        assertThat(list1.getEvents().get(0), org.hamcrest.Matchers.equalTo(obj11));
        // check the second type
        assertThat(list1.getEvents().get(1), org.hamcrest.Matchers.equalTo(obj12));
        // time diff is 2 seconds
        int diff = (int) (list1.getTimes().get(1) - list1.getTimes().get(0));
        assertThat(diff, Matchers.greaterThanOrEqualTo(1900));
    }

    @Test
    @Ignore
    public void testPublishEventPoolSizeByTwo() throws InterruptedException, AbstractAgentException {
        AgentEventListenerCollection collection = new AgentEventListenerCollection();
        AgentEventPublisherAsync async = new AgentEventPublisherAsync(collection, 2);
        List1 list1 = new List1();
        Obj1 obj11 = new Obj1();
        Obj1 obj12 = new Obj1();
        collection.add(list1);
        // publish three events
        async.publishEvent(obj11);
        async.publishEvent(obj12);
        async.publishEvent(new Obj2());
        // wait
        TimeUnit.SECONDS.sleep(5);
        // only two events are expected
        assertThat(list1.getEvents().size(), org.hamcrest.Matchers.equalTo(2));
        // check the first type
        assertThat(list1.getEvents().get(0), org.hamcrest.Matchers.equalTo(obj11));
        // check the second type
        assertThat(list1.getEvents().get(1), org.hamcrest.Matchers.equalTo(obj12));
        // time diff is less than 100 ms
        int diff = (int) (list1.getTimes().get(1) - list1.getTimes().get(0));
        assertThat(diff, Matchers.lessThan(200));
    }

    public static class List1 implements AgentEventListener<Obj1> {

        private List<Obj1> events = new ArrayList<>();
        private List<Long> times = new ArrayList<>();

        @Override
        public void onAgentEvent(Obj1 event) {
            events.add(event);
            times.add(Calendar.getInstance().getTimeInMillis());
            System.out.println(event.getSource());
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public List<Obj1> getEvents() {
            return events;
        }

        public List<Long> getTimes() {
            return times;
        }
    }

    public static class Obj1 extends AgentEvent {

        @Override
        public Object getSource() {
            return "Obj1";
        }
    }

    public static class Obj2 extends AgentEvent {

        @Override
        public Object getSource() {
            return "Obj1";
        }
    }
}
