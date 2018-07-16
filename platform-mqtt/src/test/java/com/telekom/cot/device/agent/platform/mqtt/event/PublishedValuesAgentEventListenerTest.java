package com.telekom.cot.device.agent.platform.mqtt.event;

import static org.junit.Assert.assertThat;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.telekom.cot.device.agent.platform.mqtt.PublishCallback;
import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;
import com.telekom.cot.device.agent.platform.mqtt.TemplateId;

public class PublishedValuesAgentEventListenerTest {

    @Test
    public void testProvideEvent() {
        AtomicBoolean event = new AtomicBoolean(false);
        TestPublishedValuesAgentEventListener listener = new TestPublishedValuesAgentEventListener();
        listener.provideEvent(new Consumer<String>() {

            @Override
            public void accept(String value) {
                event.set(true);
            }
        });
        listener.onAgentEvent(new TestPublishedValuesAgentEvent(null,
                        new PublishedValues(TemplateId.USED_TEMPLATE, new String[] { "id" }, new String[] { "1" })));
        assertThat(event.get(), Matchers.equalTo(true));
        assertThat(listener.isHandle(), Matchers.equalTo(true));
    }

    @Test
    public void testWaitOnAgentEventAndCreate() throws InterruptedException {
        TestPublishedValuesAgentEventListener listener = new TestPublishedValuesAgentEventListener();
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                listener.onAgentEvent(new TestPublishedValuesAgentEvent(null, new PublishedValues(
                                TemplateId.USED_TEMPLATE, new String[] { "id" }, new String[] { "1" })));
            }
        }, 2000);
        long start = Calendar.getInstance().getTimeInMillis();
        String value = listener.waitOnAgentEventAndCreate();
        long finished = Calendar.getInstance().getTimeInMillis();
        assertThat(value, Matchers.equalTo("1"));
        assertThat((finished - start) > 1999, Matchers.equalTo(true));
    }

    static class TestPublishedValuesAgentEvent extends PublishedValuesAgentEvent {

        protected TestPublishedValuesAgentEvent(PublishCallback source, PublishedValues publishedValues) {
            super(source, publishedValues);
        }
    }

    static class TestPublishedValuesAgentEventListener
                    extends PublishedValuesAgentEventListener<String, TestPublishedValuesAgentEvent> {

        private boolean handle = false;

        @Override
        void handle(String businessObject) {
            handle = businessObject != null;
        }

        public boolean isHandle() {
            return handle;
        }

        @Override
        public Class<TestPublishedValuesAgentEvent> getEventClass() {
            return TestPublishedValuesAgentEvent.class;
        }

        @Override
        public String create(PublishedValues publishedValues) {
            return publishedValues.getValue("id");
        }
    }
}
