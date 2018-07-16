package com.telekom.cot.device.agent.platform.mqtt.event;

import static org.junit.Assert.assertThat;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;
import com.telekom.cot.device.agent.platform.mqtt.TemplateId;
import com.telekom.cot.device.agent.platform.objects.Operation;

public class OperationTestOperationAgentEventListenerTest {

    @Test
    public void testWaitOnAgentEventAndCreate_EXECUTING() throws InterruptedException {
        ConcurrentLinkedQueue<Operation> pendingOperations = new ConcurrentLinkedQueue<>();
        PublishedValues publishedValues = new PublishedValues(TemplateId.OPERATION_TEST_RES,
                        new String[] { "id", "status", "c8y_TestOperation.givenStatus" },
                        new String[] { "123", "EXECUTING", "FAILED_BY_EXCEPTION" });
        OperationTestOperationAgentEvent event = new OperationTestOperationAgentEvent(null, publishedValues);
        OperationTestOperationAgentEventListener listener = new OperationTestOperationAgentEventListener(
                        pendingOperations);
        new Timer().schedule(new TimerTask() {
            
            @Override
            public void run() {
                listener.onAgentEvent(event);
            }
        }, 100);
        Operation operation = listener.waitOnAgentEventAndCreate();
        assertThat(pendingOperations.size(), Matchers.equalTo(1));
        assertThat(pendingOperations.poll(), Matchers.equalTo(operation));
    }
    
    @Test
    public void testWaitOnAgentEventAndCreate_SUCCESSFUL() throws InterruptedException {
        ConcurrentLinkedQueue<Operation> pendingOperations = new ConcurrentLinkedQueue<>();
        PublishedValues publishedValues = new PublishedValues(TemplateId.OPERATION_TEST_RES,
                        new String[] { "id", "status", "c8y_TestOperation.givenStatus" },
                        new String[] { "123", "SUCCESSFUL", "FAILED_BY_EXCEPTION" });
        OperationTestOperationAgentEvent event = new OperationTestOperationAgentEvent(null, publishedValues);
        OperationTestOperationAgentEventListener listener = new OperationTestOperationAgentEventListener(
                        pendingOperations);
        new Timer().schedule(new TimerTask() {
            
            @Override
            public void run() {
                listener.onAgentEvent(event);
            }
        }, 100);
        Operation operation = listener.waitOnAgentEventAndCreate();
        assertThat(pendingOperations.size(), Matchers.equalTo(0));
        assertThat(operation, Matchers.nullValue());
    }
}
