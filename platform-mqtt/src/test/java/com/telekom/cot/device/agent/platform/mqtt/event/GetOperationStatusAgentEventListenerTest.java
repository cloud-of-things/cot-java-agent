package com.telekom.cot.device.agent.platform.mqtt.event;

import static org.junit.Assert.assertThat;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;
import com.telekom.cot.device.agent.platform.mqtt.TemplateId;

public class GetOperationStatusAgentEventListenerTest {

    @Test
    public void testWaitOnAllAgentEvents() throws InterruptedException {
        PublishedValues publishedValues1 = new PublishedValues(TemplateId.OPERATION_TEST_RES,
                        new String[] { "id", "status" }, new String[] { "id1", "status1" });
        GetOperationStatusAgentEvent event1 = new GetOperationStatusAgentEvent(null, publishedValues1);
        PublishedValues publishedValues2 = new PublishedValues(TemplateId.OPERATION_TEST_RES,
                        new String[] { "id", "status" }, new String[] { "id2", "status2" });
        GetOperationStatusAgentEvent event2 = new GetOperationStatusAgentEvent(null, publishedValues2);
        GetOperationStatusAgentEventListener listener = new GetOperationStatusAgentEventListener();
        listener.onAgentEvent(event1);
        listener.onAgentEvent(event2);
        listener.finished();
        List<GetOperationStatus> result = listener.waitOnAllAgentEvents();
        assertThat(result.size(), Matchers.equalTo(2));
        assertThat(result.get(0).getId(),Matchers.equalTo("id1"));
        assertThat(result.get(0).getStatus(),Matchers.equalTo("status1"));
        assertThat(result.get(1).getId(),Matchers.equalTo("id2"));
        assertThat(result.get(1).getStatus(),Matchers.equalTo("status2"));
    }
}
