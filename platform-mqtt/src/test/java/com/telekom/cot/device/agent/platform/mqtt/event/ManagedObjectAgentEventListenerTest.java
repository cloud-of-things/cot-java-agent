package com.telekom.cot.device.agent.platform.mqtt.event;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;
import com.telekom.cot.device.agent.platform.mqtt.event.ManagedObjectAgentEvent;
import com.telekom.cot.device.agent.platform.mqtt.event.ManagedObjectAgentEventListener;
import com.telekom.cot.device.agent.service.event.AgentEventListenerWrapper;
import com.telekom.cot.device.agent.service.event.AgentEventUtil;

import static com.telekom.cot.device.agent.platform.mqtt.TemplateId.*;

public class ManagedObjectAgentEventListenerTest {

    @Test
    public void testOnAgentEvent() {
        // mock
        Logger mockLogger = Mockito.mock(Logger.class);
        // prepare
        PublishedValues publishedValues = new PublishedValues(CREATE_ALARM_REQ, new String[] {}, new String[] {});
        ManagedObjectAgentEvent event = new ManagedObjectAgentEvent(null, publishedValues);
        ManagedObjectAgentEventListener listener = new ManagedObjectAgentEventListener();
        // test
        AgentEventListenerWrapper wrapper = new AgentEventListenerWrapper(listener);
        InjectionUtil.inject(wrapper, mockLogger);
        wrapper.onAgentEvent(event);
        verify(mockLogger).info("is the event type equals listener type result={}", true);
        // check by AgentEventUtil
        assertThat(AgentEventUtil.getEventType(listener), Matchers.equalTo(ManagedObjectAgentEvent.class));
    }
}
