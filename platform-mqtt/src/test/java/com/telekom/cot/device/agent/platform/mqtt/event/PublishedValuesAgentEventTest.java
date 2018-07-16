package com.telekom.cot.device.agent.platform.mqtt.event;

import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;
import com.telekom.cot.device.agent.platform.mqtt.TemplateId;
import com.telekom.cot.device.agent.platform.mqtt.event.ManagedObjectAgentEvent;
import com.telekom.cot.device.agent.platform.mqtt.event.ManagedObjectAgentEventListener;
import com.telekom.cot.device.agent.platform.mqtt.event.PublishedValuesAgentEvent;
import com.telekom.cot.device.agent.service.event.AgentEventListenerCollection;
import com.telekom.cot.device.agent.service.event.AgentEventPublisher;
import com.telekom.cot.device.agent.service.event.AgentEventPublisherAsync;

public class PublishedValuesAgentEventTest {

    private AgentEventListenerCollection agentEventListenerCollection = null;
    private ManagedObjectAgentEventListener managedObjectAgentEventListener = null;
    private AgentEventPublisher agentEventPublisher = null;

    @Before
    public void setUp() throws AbstractAgentException {
        agentEventListenerCollection = new AgentEventListenerCollection();
        agentEventPublisher = new AgentEventPublisherAsync(agentEventListenerCollection, 1);
        managedObjectAgentEventListener = new ManagedObjectAgentEventListener();
        agentEventListenerCollection.add(managedObjectAgentEventListener);
    }

    @Test
    public void test() throws AbstractAgentException, InterruptedException {
        PublishedValues publishedValues = new PublishedValues(TemplateId.GET_MANAGED_OBJECT_ID_RES, //
                        new String[] { "externalId", "managedObject.id" }, new String[] { "123", "456" });
        PublishedValuesAgentEvent agentEvent = PublishedValuesAgentEvent
                        .createEvent(ManagedObjectAgentEvent.class, null, publishedValues);
        agentEventPublisher.publishEvent(agentEvent);
        assertThat(managedObjectAgentEventListener.waitOnAgentEventAndCreate().getId(), Matchers.equalTo("456"));
    }
}
